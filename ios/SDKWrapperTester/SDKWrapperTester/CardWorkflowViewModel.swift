/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation

enum WorkflowType {
	case authentication,
	     changePin,
	     changeTransportPin
}

enum ViewState {
	case loadCertificate,
	     abort,
	     accessRights,
	     enterPin,
	     enterTransportPin,
	     enterCan,
	     enterPuk,
	     changePin,
	     error,
	     cardRequested,
	     unknownCard,
	     pause
}

enum PasswordType {
	case pin,
	     transportPin,
	     can,
	     puk
}

public enum SimulatorMode {
	case disabled,
	     defaultData,
	     differentFirstName,
	     differentPseudonym
}

class CardWorkflowViewModel: ObservableObject {
	@Published var state: ViewState

	@Published var accessRights: AccessRights?
	@Published var certificateDescription: CertificateDescription?

	@Published var currentErrorMessage: String?

	@Published var lastCard: Card?

	@Published var developerMode: Bool = false
	@Published var userInfoMessages: AA2UserInfoMessages?
	@Published var lastAttempt: PasswordType?

	@Published var workflowProgress: WorkflowProgress = .init()

	private let workflowType: WorkflowType

	private var authResult: AuthResult?

	var tcTokenURL: URL?

	var simulatorMode: SimulatorMode = .disabled

	var onFinished: ((Any?) -> Void)?

	init(workflowType: WorkflowType) {
		self.workflowType = workflowType

		switch workflowType {
		case .authentication:
			state = .loadCertificate

		case .changePin,
		     .changeTransportPin:
			state = .cardRequested
		}

		AA2SDKWrapper.workflowController.registerCallbacks(self)
		AA2SDKWrapper.workflowController.start()
	}

	func cancelWorkflow() {
		AA2SDKWrapper.workflowController.cancel()
		if workflowType == .authentication {
			state = .abort
		}
	}

	func acceptAccessRights() {
		AA2SDKWrapper.workflowController.accept()
	}

	func onSetAccessRights(optionalRights: [AccessRight]) {
		AA2SDKWrapper.workflowController.setAccessRights(optionalRights)
	}

	func setPin(pin: String) {
		lastAttempt = (workflowType == .changeTransportPin ? .transportPin : .pin)
		AA2SDKWrapper.workflowController.setPin(pin)
	}

	func setCan(can: String) {
		lastAttempt = .can
		AA2SDKWrapper.workflowController.setCan(can)
	}

	func setPuk(puk: String) {
		lastAttempt = .puk
		AA2SDKWrapper.workflowController.setPuk(puk)
	}

	func setNewPin(newPin: String) {
		AA2SDKWrapper.workflowController.setNewPin(newPin)
	}

	func setErrorState(error: String) {
		currentErrorMessage = error
		state = .error
	}

	func acceptError() {
		if state == .unknownCard {
			state = .cardRequested
		} else if state == .pause {
			state = .cardRequested
			AA2SDKWrapper.workflowController.continueWorkflow()
		} else {
			finishWithResult(result: authResult)
		}
	}

	private func finishWithResult(result: Any?) {
		if let onFinished = onFinished {
			onFinished(result)
		}
		cleanup()
	}

	private func cleanup() {
		AA2SDKWrapper.workflowController.unregisterCallbacks(self)

		if AA2SDKWrapper.workflowController.isStarted {
			AA2SDKWrapper.workflowController.stop()
		}
	}
}

extension CardWorkflowViewModel: WorkflowCallbacks {
	func onPause(cause: Cause) {
		currentErrorMessage = "The workflow was interrupted: \"\(cause)\""
		state = .pause
	}

	func onInfo(versionInfo: VersionInfo) {
		print("VersionInfo about AusweisApp2: \(versionInfo)")
	}

	func onReaderList(readers: [Reader]?) {
		print("Got readerList: \(String(describing: readers))")
	}

	func onReader(reader: Reader?) {
		print("Got reader: \(String(describing: reader))")

		if let card = reader?.card {
			if card.deactivated ?? false {
				setErrorState(error: "The card is deactivated")
			}

			if card.inoperative ?? false {
				setErrorState(error: "The card is inoperative")
			}

			if card.isUnknown() {
				currentErrorMessage = "The card is unknown (non-eID)"
				state = .unknownCard
			}
		}
	}

	func onStarted() {
		AA2SDKWrapper.workflowController.getInfo()
		AA2SDKWrapper.workflowController.getReader(name: "Simulator")
		AA2SDKWrapper.workflowController.getReader(name: "Unknown")
		AA2SDKWrapper.workflowController.getReaderList()

		switch workflowType {
		case .authentication:
			if let tcTokenUrl = tcTokenURL {
				AA2SDKWrapper.workflowController.startAuthentication(withTcTokenUrl: tcTokenUrl,
				                                                     withDeveloperMode: developerMode,
				                                                     withUserInfoMessages: userInfoMessages)
			}
		case .changePin,
		     .changeTransportPin:
			AA2SDKWrapper.workflowController.startChangePin(withUserInfoMessages: userInfoMessages)
		}
	}

	func onAuthenticationStarted() {
		// The authentication was started successfully, nothing to do here.
	}

	func onAuthenticationStartFailed(error: String) {
		setErrorState(error: error)
	}

	func onChangePinStarted() {
		// The PIN change was started successfully, nothing to do here.
	}

	func onAccessRights(error: String?, accessRights: AccessRights?) {
		if let error = error {
			setErrorState(error: error)
			return
		}

		if self.accessRights != nil {
			// When setting the effective rights via setAccessRights(), the SDK
			// will sent the "new" effective rights again, accept those.
			acceptAccessRights()
			return
		}

		self.accessRights = accessRights
		AA2SDKWrapper.workflowController.getCertificate()
		state = .accessRights
	}

	func onCertificate(certificateDescription: CertificateDescription) {
		self.certificateDescription = certificateDescription
	}

	func onInsertCard(error: String?) {
		currentErrorMessage = error
		state = error != nil ? .error : .cardRequested

		if error != nil || simulatorMode == .disabled {
			return
		}

		// swiftlint:disable line_length
		if simulatorMode == .defaultData {
			AA2SDKWrapper.workflowController.setCard(name: "Simulator")
		} else if simulatorMode == .differentFirstName {
			let simulatorFiles: [SimulatorFile] = [
				SimulatorFile(withFileId: "0103", withShortFileId: "03", withContent: "630a12083230323931303331"),
				SimulatorFile(withFileId: "0104", withShortFileId: "04", withContent: "64060c044552494b") // ERIK
			]
			AA2SDKWrapper.workflowController.setCard(name: "Simulator", simulator: Simulator(withFiles: simulatorFiles))
		} else if simulatorMode == .differentPseudonym {
			let simulatorFiles: [SimulatorFile] = [
				SimulatorFile(withFileId: "0103", withShortFileId: "03", withContent: "630a12083230323931303331"),
				SimulatorFile(withFileId: "0104", withShortFileId: "04", withContent: "64070c054552494b41")
			]
			let keys = [
				SimulatorKey(withId: 2, withContent: "308201610201003081ec06072a8648ce3d02013081e0020101302c06072a8648ce3d0101022100a9fb57dba1eea9bc3e660a909d838d726e3bf623d52620282013481d1f6e5377304404207d5a0975fc2c3057eef67530417affe7fb8055c126dc5c6ce94a4b44f330b5d9042026dc5c6ce94a4b44f330b5d9bbd77cbf958416295cf7e1ce6bccdc18ff8c07b60441048bd2aeb9cb7e57cb2c4b482ffc81b7afb9de27e1e3bd23c23a4453bd9ace3262547ef835c3dac4fd97f8461a14611dc9c27745132ded8e545c1d54c72f046997022100a9fb57dba1eea9bc3e660a909d838d718c397aa3b561a6f7901e0e82974856a7020101046d306b020101042005eefab8d4e0bb6a0db1e587ddc81838546cab90013ab95186a1033116526af2a144034200046e5e1c5f6b36b4b5ce6a82d71c753fdc6bb0efc7a93c4ac71201e05f5b77c2a274d50e134ec6f362f93eed7c1b81abd7c187df60aab6c2a726b6e62e39d4aa9f")
			]
			AA2SDKWrapper.workflowController.setCard(name: "Simulator", simulator: Simulator(withFiles: simulatorFiles, withKeys: keys))
		}
		// swiftlint:enable line_length
	}

	func onEnterPin(error: String?, reader: Reader) {
		lastCard = reader.card

		if reader.keypad && error == nil {
			AA2SDKWrapper.workflowController.setPin(nil)
			return
		}

		AA2SDKWrapper.workflowController.interrupt()
		lastCard = reader.card
		currentErrorMessage = error
		if error != nil {
			state = .error
		} else if workflowType == .changeTransportPin {
			state = .enterTransportPin
		} else {
			state = .enterPin
		}
	}

	func onEnterNewPin(error: String?, reader: Reader) {
		lastCard = reader.card
		currentErrorMessage = error
		state = error != nil ? .error : .changePin

		if reader.keypad && error == nil {
			AA2SDKWrapper.workflowController.setNewPin(nil)
			return
		}

		AA2SDKWrapper.workflowController.interrupt()
	}

	func onEnterPuk(error: String?, reader: Reader) {
		lastCard = reader.card
		currentErrorMessage = error

		if reader.keypad && error == nil {
			AA2SDKWrapper.workflowController.setPuk(nil)
			return
		}

		AA2SDKWrapper.workflowController.interrupt()
		state = error != nil ? .error : .enterPuk
	}

	func onEnterCan(error: String?, reader: Reader) {
		lastCard = reader.card
		currentErrorMessage = error

		if reader.keypad && error == nil {
			AA2SDKWrapper.workflowController.setCan(nil)
			return
		}

		AA2SDKWrapper.workflowController.interrupt()
		state = error != nil ? .error : .enterCan
	}

	func onAuthenticationCompleted(authResult: AuthResult) {
		self.authResult = authResult

		let isCancellationByUser = authResult.result?.isCancellationByUser ?? false

		if !authResult.hasError || isCancellationByUser {
			finishWithResult(result: authResult)
		} else {
			setErrorState(error: authResult.result?.message ??
				NSLocalizedString(
					"An unknown error occurred during authentication.",
					comment: "Authentication error message was empty"
				))
		}
	}

	func onChangePinCompleted(changePinResult: ChangePinResult) {
		finishWithResult(result: changePinResult)
	}

	func onWrapperError(error: WrapperError) {
		setErrorState(error: error.error)
	}

	func onBadState(error: String) {
		setErrorState(error: error)
	}

	func onInternalError(error: String) {
		setErrorState(error: error)
	}

	func onStatus(workflowProgress: WorkflowProgress) {
		self.workflowProgress = workflowProgress
	}
}
