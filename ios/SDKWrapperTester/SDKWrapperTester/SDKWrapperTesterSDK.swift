/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

public enum SDKWrapperTesterSDK {
	public static var sdkTheme = SDKWrapperTesterTheme()

	public static func authenticate(
		withTcTokenUrl tcTokenUrl: URL,
		parentViewController: UIViewController,
		developerMode: Bool = false,
		userInfoMessages: AA2UserInfoMessages? = nil,
		simulatorMode: SimulatorMode = .disabled,
		completion: @escaping (AuthResult?) -> Void
	) {
		let viewModel = CardWorkflowViewModel(workflowType: .authentication)
		viewModel.tcTokenURL = tcTokenUrl
		viewModel.developerMode = developerMode
		viewModel.userInfoMessages = userInfoMessages
		viewModel.simulatorMode = simulatorMode

		let contentView = CardWorkflowView()
			.environmentObject(sdkTheme)
			.environmentObject(viewModel)

		let hostingController = UIHostingController(rootView: contentView)
		hostingController.modalPresentationStyle = .fullScreen

		viewModel.onFinished = { [weak hostingController] result in
			hostingController?.dismiss(animated: true)
			completion(result as? AuthResult)
		}

		parentViewController.present(hostingController, animated: true)
	}

	public static func changePin(
		parentViewController: UIViewController,
		userInfoMessages: AA2UserInfoMessages? = nil,
		simulatorMode: SimulatorMode = .disabled,
		completion: @escaping ((ChangePinResult) -> Void)
	) {
		let viewModel = CardWorkflowViewModel(workflowType: .changePin)
		viewModel.userInfoMessages = userInfoMessages
		viewModel.simulatorMode = simulatorMode

		let contentView = CardWorkflowView()
			.environmentObject(sdkTheme)
			.environmentObject(viewModel)

		let hostingController = UIHostingController(rootView: contentView)
		hostingController.modalPresentationStyle = .fullScreen

		viewModel.onFinished = { [weak hostingController] result in
			hostingController?.dismiss(animated: true)
			if let result = result as? ChangePinResult {
				completion(result)
			}
		}

		parentViewController.present(hostingController, animated: true)
	}

	public static func changeTransportPin(
		parentViewController: UIViewController,
		completion: @escaping ((ChangePinResult) -> Void)
	) {
		let viewModel = CardWorkflowViewModel(workflowType: .changeTransportPin)

		let contentView = CardWorkflowView()
			.environmentObject(sdkTheme)
			.environmentObject(viewModel)

		let hostingController = UIHostingController(rootView: contentView)
		hostingController.modalPresentationStyle = .fullScreen

		viewModel.onFinished = { [weak hostingController] result in
			hostingController?.dismiss(animated: true)
			if let result = result as? ChangePinResult {
				completion(result)
			}
		}

		parentViewController.present(hostingController, animated: true)
	}
}
