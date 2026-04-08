/*
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import Network
import Swifter

class WebsocketModeModel: ObservableObject {
	@Published var isStarted: Bool = false
	@Published var logMessages: [LogMessage] = []

	private let serverPort: UInt16 = 8080

	private var websocketSession: WebSocketSession?
	private lazy var server: HttpServer = {
		let server = HttpServer()
		server["/"] = websocket(text: { [weak self] _, text in
			self?.websocketDispatch(message: text)
		}, connected: { [weak self] session in
			self?.log("Client connected")
			self?.websocketSession = session
		}, disconnected: { [weak self] _ in
			self?.log("Client disconnected")
		})
		return server
	}()

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

	func start() {
		if isStarted {
			return
		}

		isStarted = true
		logMessages = []

		DispatchQueue.global(qos: .utility).async {
			do {
				try self.server.start(self.serverPort, forceIPv4: true)
				self.log("Server has started.")
				self.log("Send ':help' for the command list")
			} catch {
				self.log("Could not start server")
				self.isStarted = false
				return
			}

			self.wifiPathMonitor.start(queue: DispatchQueue.global(qos: .utility))
		}
	}

	func stop() {
		if !isStarted {
			return
		}

		log("Server has stopped")

		isStarted = false
		websocketSession = nil
		server.stop()
		AusweisApp2SDK.shared.stop()
		wifiPathMonitor.cancel()
	}

	private func log(_ message: String) {
		DispatchQueue.main.async {
			self.logMessages.append(LogMessage(text: message))
		}
	}

	private func send(_ message: String) {
		log(message)
		websocketSession?.writeText(message)
	}

	private func ausweisAppDispatch(message: String?) {
		guard let message = message else {
			send("AusweisApp2 started")
			return
		}

		log(message)
		send(message)
	}

	private func websocketDispatch(message: String) {
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
