/**
 * Copyright (c) 2020-2025 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2
import Foundation

class AA2SdkConnection: SdkConnection {
	static let shared = AA2SdkConnection()

	var isStarted: Bool {
		ausweisapp_is_running()
	}

	var onConnected: (() -> Void)?
	var onMessageReceived: ((_ message: AA2Message) -> Void)?

	private let ausweisAppCallback: AusweisAppCallback = { (msg: UnsafePointer<CChar>?) in
		guard let msg = msg else {
			AA2SdkConnection.shared.onNewMessage(messageJson: nil)
			return
		}
		let messageString = String(cString: msg)
		AA2SdkConnection.shared.onNewMessage(messageJson: messageString)
	}

	private let jsonDecoder = JSONDecoder()
	private let jsonEncoder = JSONEncoder()

	private init() {
		// Prevent instantiation from outside of AA2SdkConnection as it is a Singleton.
	}

	func start() {
		ausweisapp_init(ausweisAppCallback, nil)
	}

	func stop() {
		ausweisapp_shutdown()
	}

	func send<T: Command>(command: T) {
		do {
			let messageData = try jsonEncoder.encode(command)
			if let messageJson = String(data: messageData, encoding: .utf8) {
				ausweisapp_send(messageJson)
			}
		} catch {
			print("Could not send json message")
		}
	}

	private func onNewMessage(messageJson: String?) {
		guard let messageJson = messageJson else {
			if let onConnected = onConnected {
				onConnected()
			}
			return
		}

		print("Received message: \(messageJson)")
		do {
			let messageData = Data(messageJson.utf8)
			let message = try jsonDecoder.decode(AA2Message.self, from: messageData)

			if let onMessageReceived = onMessageReceived {
				onMessageReceived(message)
			}
		} catch {
			print("Could not parse json message")
		}
	}
}
