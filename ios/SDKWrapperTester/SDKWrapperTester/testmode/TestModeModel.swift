/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation

class TestModeModel: ObservableObject {
	struct Command: Encodable {
		init(
			cmd: String,
			value: String? = nil,
			tcTokenURL: String? = nil,
			messages: Messages? = nil,
			handleInterrupt: Bool? = nil,
			level: Int? = nil
		) {
			self.cmd = cmd
			self.value = value
			self.tcTokenURL = tcTokenURL
			self.messages = messages
			self.handleInterrupt = handleInterrupt
			self.level = level
		}

		let cmd: String
		let value: String?
		let tcTokenURL: String?
		let messages: Messages?
		let handleInterrupt: Bool?
		let level: Int?
	}

	struct Messages: Encodable {
		let sessionStarted: String
		let sessionFailed: String
		let sessionSucceeded: String
		let sessionInProgress: String
	}

	struct Card: Decodable {
		let inoperative: Bool?
		let deactivated: Bool?
		let retryCounter: Int?

		public func isEmpty() -> Bool {
			return inoperative == nil && deactivated == nil && retryCounter == nil
		}
	}

	struct Message: Decodable {
		let msg: String
		let error: String?
		let url: String?
		let card: Card?
		let cause: String?
	}

	private let jsonDecoder = JSONDecoder()
	private let jsonEncoder = JSONEncoder()

	@Published var isStarted: Bool = false
	@Published var handleInterrupt: Bool = true
	@Published var useCustomMessages: Bool = true
	@Published var interruptScan: Bool = false
	@Published var logMessages: [LogMessage] = []
	@Published var redirectUrl: URL?

	private var pin: String?
	private var tcTokenUrl: String?
	private var apiLevel: Int?

	// swiftlint: disable function_parameter_count
	public func authenticate(
		to tcTokenUrl: String,
		withPin pin: String,
		useCustomMessages: Bool,
		handleInterrupt: Bool,
		interruptScan: Bool,
		apiLevel: String
	) {
		if isStarted {
			return
		}

		isStarted = true
		self.useCustomMessages = useCustomMessages
		self.handleInterrupt = handleInterrupt
		self.interruptScan = interruptScan
		logMessages = []

		self.tcTokenUrl = tcTokenUrl
		self.pin = pin
		self.apiLevel = Int(apiLevel)
		AusweisApp2SDK.shared.start(onNewMessage: { [weak self] in self?.dispatch(message: $0) })
	}

	// swiftlint: enable function_parameter_count

	public func stop() {
		if !isStarted {
			return
		}

		AusweisApp2SDK.shared.stop()
		isStarted = false
		pin = nil
		tcTokenUrl = nil
		redirectUrl = nil
		apiLevel = nil
	}

	public func setPin() {
		send(Command(cmd: "SET_PIN", value: pin))
	}

	private func log(_ message: String) {
		logMessages.append(LogMessage(text: message))
	}

	private func send(_ command: Command) {
		do {
			let messageData = try jsonEncoder.encode(command)
			if let message = String(data: messageData, encoding: .utf8) {
				log(message)
				AusweisApp2SDK.shared.send(message: message)
			}
		} catch {
			log("Could not encode json message")
		}
	}

	private func sendRunAuth() {
		send(Command(
			cmd: "RUN_AUTH",
			tcTokenURL: tcTokenUrl,
			messages: useCustomMessages ? Messages(
				sessionStarted: "SDKTester: sessionStarted - Please place ID Card on device",
				sessionFailed: "SDKTester: sessionFailed",
				sessionSucceeded: "SDKTester: sessionSucceeded",
				sessionInProgress: "SDKTester: sessionInProgress - "
			) : nil,
			handleInterrupt: handleInterrupt
		))
	}

	// swiftlint: disable cyclomatic_complexity function_body_length
	private func dispatch(message: String?) {
		var msg: Message?
		if let message = message {
			do {
				msg = try jsonDecoder.decode(Message.self, from: Data(message.utf8))
			} catch {
				log("Could not parse json message \(message)")
			}
		}

		log(message ?? "AusweisApp2 started")

		switch msg?.msg {
		case nil:
			send(Command(cmd: "SET_API_LEVEL", level: apiLevel))
		case "API_LEVEL":
			if msg?.error == nil {
				sendRunAuth()
			} else {
				stop()
			}
		case "READER":
			if let card = msg?.card {
				if card.isEmpty() {
					log("Unknown card detected")
					break
				}
				log("eID card detected")
			}
		case "PAUSE":
			if let cause = msg?.cause {
				log("Received message PAUSE with cause \(cause)")
			}
			send(Command(cmd: "CONTINUE"))
		case "ACCESS_RIGHTS":
			send(Command(cmd: "ACCEPT"))
		case "ENTER_PIN":
			if !handleInterrupt && interruptScan {
				send(Command(cmd: "INTERRUPT"))
			} else {
				send(Command(cmd: "SET_PIN", value: pin))
			}
		case "AUTH":
			if msg?.error != nil || msg?.url != nil {
				stop()

				if let url = msg?.url {
					redirectUrl = URL(string: url)
				} else {
					redirectUrl = nil
				}
			}
		default:
			break
		}
	}
	// swiftlint: enable cyclomatic_complexity function_body_length
}
