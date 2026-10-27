/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
 */

@testable import AusweisApp2SDKWrapper
import XCTest

class TestWorkflowCallbacks: WorkflowCallbacks {
	var doOnStarted: (() -> Void)?
	var doOnAuthenticationStarted: (() -> Void)?
	var doOnChangePinStarted: (() -> Void)?
	var doOnRequestAccessRights: ((AccessRights) -> Void)?
	var doOnRequestCard: (() -> Void)?
	var doOnRecognizedCard: ((Card?) -> Void)?
	var doOnRequestPin: ((Card) -> Void)?
	var doOnRequestNewPin: ((Card) -> Void)?
	var doOnAuthenticationCompleted: ((AuthResult) -> Void)?
	var doOnChangePinCompleted: ((ChangePinResult) -> Void)?
	var doOnError: (() -> Void)?

	func onInfo(versionInfo _: VersionInfo) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onReader(reader: Reader?) {
		if let doOnRecognizedCard, let card = reader?.card {
			DispatchQueue.global().async {
				doOnRecognizedCard(card)
			}
		}
	}

	func onReaderList(readers _: [Reader]?) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onStarted() {
		if let doOnStarted {
			DispatchQueue.global().async {
				doOnStarted()
			}
		}
	}

	func onAuthenticationStarted() {
		if let doOnAuthenticationStarted {
			DispatchQueue.global().async {
				doOnAuthenticationStarted()
			}
		}
	}

	func onChangePinStarted() {
		if let doOnChangePinStarted {
			DispatchQueue.global().async {
				doOnChangePinStarted()
			}
		}
	}

	func onAccessRights(error _: String?, accessRights: AccessRights?) {
		if accessRights == nil {
			return
		}

		if let doOnRequestAccessRights {
			DispatchQueue.global().async {
				doOnRequestAccessRights(accessRights!)
			}
		}
	}

	func onCertificate(certificateDescription _: CertificateDescription) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onInsertCard(error _: String?) {
		if let doOnRequestCard {
			DispatchQueue.global().async {
				doOnRequestCard()
			}
		}
	}

	func onRecognizedCard(card _: Card?) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onPause(cause _: Cause) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onEnterPin(error _: String?, reader: Reader) {
		if let doOnRequestPin, let card = reader.card {
			DispatchQueue.global().async {
				doOnRequestPin(card)
			}
		}
	}

	func onEnterNewPin(error _: String?, reader: Reader) {
		if let doOnRequestNewPin, let card = reader.card {
			DispatchQueue.global().async {
				doOnRequestNewPin(card)
			}
		}
	}

	func onEnterPuk(error _: String?, reader _: Reader) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onEnterCan(error _: String?, reader _: Reader) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onAuthenticationCompleted(authResult: AuthResult) {
		if let doOnAuthenticationCompleted {
			DispatchQueue.global().async {
				doOnAuthenticationCompleted(authResult)
			}
		}
	}

	func onAuthenticationStartFailed(error _: String) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onChangePinCompleted(changePinResult: ChangePinResult) {
		if let doOnChangePinCompleted {
			DispatchQueue.global().async {
				doOnChangePinCompleted(changePinResult)
			}
		}
	}

	func onStatus(workflowProgress _: WorkflowProgress) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onWrapperError(error _: AusweisApp2SDKWrapper.WrapperError) {
		if let doOnError {
			DispatchQueue.global().async {
				doOnError()
			}
		}
	}

	func onBadState(error _: String) {
		// The function is required by the protocol and will be implemented by the unittests.
	}

	func onInternalError(error _: String) {
		// The function is required by the protocol and will be implemented by the unittests.
	}
}
