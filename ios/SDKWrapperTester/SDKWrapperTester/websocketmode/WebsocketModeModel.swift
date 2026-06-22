/*
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import Network

class WebsocketModeModel: ObservableObject {
	@Published var isStarted: Bool = false
	@Published var logMessages: [LogMessage] = []

	private let serverPort: UInt16 = 8080
	private var listener: NWListener?
	private var connection: NWConnection?

	private lazy var wifiPathMonitor: NWPathMonitor = {
		let pathMon = NWPathMonitor(requiredInterfaceType: .wifi)
		pathMon.pathUpdateHandler = { nwPath in
			if let interface = nwPath.availableInterfaces.first {
				let wifiIP = self.getIPAddress(for: interface.name)
				self.log("Websocket: ws://\(wifiIP):\(self.serverPort)")
			}
		}
		return pathMon
	}()

	private func log(_ message: String) {
		DispatchQueue.main.async {
			self.logMessages.append(LogMessage(text: message))
		}
	}

	func start() {
		if isStarted {
			return
		}

		logMessages = []

		let parameters = NWParameters.tcp
		let wsOptions = NWProtocolWebSocket.Options()
		parameters.defaultProtocolStack.applicationProtocols.insert(wsOptions, at: 0)

		do {
			listener = try NWListener(using: parameters, on: NWEndpoint.Port(rawValue: serverPort)!)
		} catch {
			log("Failed to create listener: \(error)")
			return
		}

		listener?.stateUpdateHandler = handleListenerState
		listener?.newConnectionHandler = handleConnection
		listener?.start(queue: .main)

		DispatchQueue.global(qos: .utility).async {
			self.wifiPathMonitor.start(queue: DispatchQueue.global(qos: .utility))
		}
		isStarted = true
	}

	func stop() {
		if !isStarted {
			return
		}

		log("Server has stopped")

		listener?.cancel()
		listener = nil
		connection?.cancel()
		connection = nil
		isStarted = false
		AusweisApp2SDK.shared.stop()
		wifiPathMonitor.cancel()
	}

	private func handleListenerState(_ newState: NWListener.State) {
		switch newState {
		case .ready:
			log("Server has started.")
			log("Send ':help' for the command list")
		case let .failed(error):
			log("Could not start server: \(error)")
			isStarted = false
		default:
			break
		}
	}

	private func handleConnection(_ connection: NWConnection) {
		if self.connection != nil {
			log("Connection already present, ignoring \(connection.endpoint)")
			return
		}

		self.connection = connection
		log("Client connected: \(connection.endpoint)")
		connection.stateUpdateHandler = handleConnectionState
		connection.start(queue: .main)
	}

	private func handleConnectionState(_ state: NWConnection.State) {
		switch state {
		case .ready:
			receive()
		case let .failed(error):
			if connection == nil {
				log("Connection failed: \(error)")
				return
			}

			switch error {
			case .posix(POSIXErrorCode.ENOTCONN),
			     .posix(POSIXErrorCode.ETIMEDOUT),
			     .posix(POSIXErrorCode.ECONNRESET):
				log("Client disconnected")
			default:
				log("Connection closed: \(error)")
			}

			connection = nil
		case .cancelled:
			log("Connection cancelled")
			connection = nil
		default:
			break
		}
	}

	private func receive() {
		connection?.receiveMessage { [weak self] data, context, isComplete, error in
			self?.receiveMessage(data, context, isComplete, error)
		}
	}

	private func receiveMessage(_ content: Data?, _: NWConnection.ContentContext?, _: Bool, _ error: NWError?) {
		if case .posix(POSIXError.ENOTCONN) = error {
			return
		}

		if let error {
			log("Receive error: \(error)")
			receive()
			return
		}

		if let content, !content.isEmpty {
			websocketDispatch(content)
		}

		receive()
	}

	private func websocketDispatch(_ data: Data) {
		let message = String(data: data, encoding: .utf8) ?? ""
		log(message)

		switch message.trimmingCharacters(in: .whitespacesAndNewlines) {
		case ":open":
			if AusweisApp2SDK.shared.isStarted {
				send("Already connected")
			} else {
				send("Starting SDK...")
				AusweisApp2SDK.shared.start(onNewMessage: { [weak self] in self?.ausweisAppDispatch(message: $0) })
			}
		case ":close":
			if AusweisApp2SDK.shared.isStarted {
				send("Stopping SDK...")
				AusweisApp2SDK.shared.stop()
			} else {
				send("Not connected yet")
			}
		case ":help":
			send("--------Status--------")
			send("SDK isStarted: \(AusweisApp2SDK.shared.isStarted)")
			send("---------Help---------")
			send(":open - Open connection to SDK")
			send(":close - Close connection to SDK")
			send(":help - This help")
		default:
			if AusweisApp2SDK.shared.isStarted {
				AusweisApp2SDK.shared.send(message: message)
			} else {
				send("Not connected to SDK! Try :help for commands.")
			}
		}
	}

	private func send(_ message: String) {
		log(message)

		let metadata = NWProtocolWebSocket.Metadata(opcode: .text)
		let context = NWConnection.ContentContext(identifier: "text", metadata: [metadata])

		connection?.send(content: message.data(using: .utf8), contentContext: context,
		                 isComplete: true, completion: .idempotent)
	}

	private func ausweisAppDispatch(message: String?) {
		guard let message else {
			send("AusweisApp started")
			return
		}

		send(message)
	}

	func getIPAddress(for interace: String) -> String {
		var address: String?
		var ifaddr: UnsafeMutablePointer<ifaddrs>?
		if getifaddrs(&ifaddr) == 0 {
			var ptr = ifaddr
			while ptr != nil {
				defer { ptr = ptr?.pointee.ifa_next }
				guard let interface = ptr?.pointee else { return "" }
				let addrFamily = interface.ifa_addr.pointee.sa_family
				if addrFamily == UInt8(AF_INET) {
					let name = String(cString: interface.ifa_name)
					if name == interace {
						var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
						getnameinfo(
							interface.ifa_addr,
							socklen_t(interface.ifa_addr.pointee.sa_len),
							&hostname, socklen_t(hostname.count),
							nil,
							socklen_t(0),
							NI_NUMERICHOST
						)
						address = String(cString: hostname)
					}
				}
			}
			freeifaddrs(ifaddr)
		}
		return address ?? ""
	}
}
