/*
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2
import Foundation

class AusweisApp2SDK {
	static let shared = AusweisApp2SDK()

	var isStarted: Bool {
		ausweisapp_is_running()
	}

	private var onNewMessage: ((_ message: String?) -> Void)?

	private init() {
		// Prevent instantiation from outside of AusweisApp2SDK as it is a Singleton.
	}

	func start(onNewMessage: @escaping ((_ message: String?) -> Void)) {
		if isStarted {
			return
		}

		self.onNewMessage = onNewMessage

		DispatchQueue.global().async {
			ausweisapp_init({ (cString: UnsafePointer<CChar>?) in
				if let cString = cString {
					AusweisApp2SDK.shared.dispatch(message: String(cString: cString))
				} else {
					AusweisApp2SDK.shared.dispatch(message: nil)
				}
			}, nil)
		}
	}

	func stop() {
		if !isStarted {
			return
		}

		onNewMessage = nil

		DispatchQueue.global().async {
			ausweisapp_shutdown()
		}
	}

	func send(message: String) {
		DispatchQueue.global().async {
			ausweisapp_send(message)
		}
	}

	private func dispatch(message: String?) {
		DispatchQueue.main.async {
			if let onNewMessage = self.onNewMessage {
				onNewMessage(message)
			}
		}
	}
}
