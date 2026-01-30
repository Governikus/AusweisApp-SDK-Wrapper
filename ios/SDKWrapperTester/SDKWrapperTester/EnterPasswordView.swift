/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

struct EnterPasswordView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	@EnvironmentObject var viewModel: CardWorkflowViewModel

	@State var passwordType: PasswordType {
		didSet {
			password.maxLength = passwordLength
		}
	}

	@ObservedObject private var password = MaxLengthString()

	private var retryCounter: Int {
		viewModel.lastCard?.pinRetryCounter ?? -1
	}

	private var lastAttemptSuccessful: Bool {
		viewModel.lastAttempt == nil ||
			(passwordType == .pin && viewModel.lastAttempt != .pin) ||
			(passwordType == .transportPin && viewModel.lastAttempt != .transportPin) ||
			(passwordType == .can && viewModel.lastAttempt != .can) ||
			(passwordType == .puk && viewModel.lastAttempt != .puk)
	}

	private var title: String {
		switch passwordType {
		case .pin:
			return NSLocalizedString("Please enter the PIN.", comment: "PIN CTA")
		case .transportPin:
			return NSLocalizedString("Please enter the Transport PIN.", comment: "Transport PIN CTA")
		case .puk:
			return NSLocalizedString("Please enter the PUK.", comment: "PUK CTA")
		case .can:
			return NSLocalizedString("Please enter the CAN.", comment: "CAN CTA")
		}
	}

	private var navigationTitle: String {
		switch passwordType {
		case .pin,
		     .transportPin:
			return NSLocalizedString("PIN required", comment: "PIN navigation title")
		case .puk:
			return NSLocalizedString("PUK required", comment: "PUK navigation title")
		case .can:
			return NSLocalizedString("CAN required", comment: "CAN navigation title")
		}
	}

	private var info: String {
		switch passwordType {
		case .pin,
		     .transportPin:
			return ""
		case .puk:
			return NSLocalizedString(
				"The ID card is blocked! Please enter the PUK to unblock it.",
				comment: "PUK info"
			)
		case .can:
			return NSLocalizedString(
				"The CAN is needed to continue with the workflow. You can find the 6-digit CAN on the front of the ID card.",
				comment: "CAN info"
			)
		}
	}

	private var passwordPlaceholder: String {
		switch passwordType {
		case .pin:
			return NSLocalizedString("6-digit PIN", comment: "PIN input placeholder")
		case .transportPin:
			return NSLocalizedString("5-digit PIN", comment: "Transport PIN input placeholder")
		case .puk:
			return NSLocalizedString("10-digit PUK", comment: "PUK input placeholder")
		case .can:
			return NSLocalizedString("6-digit CAN", comment: "CAN input placeholder")
		}
	}

	private var passwordLength: Int {
		switch passwordType {
		case .pin:
			return WorkflowController.pinLength
		case .transportPin:
			return WorkflowController.transportPinLength
		case .can:
			return WorkflowController.canLength
		case .puk:
			return WorkflowController.pukLength
		}
	}

	private var validPassword: Bool {
		return password.text.count == passwordLength
	}

	private func acceptPassword() {
		switch passwordType {
		case .pin,
		     .transportPin:
			viewModel.setPin(pin: password.text)
		case .puk:
			viewModel.setPuk(puk: password.text)
		case .can:
			viewModel.setCan(can: password.text)
		}
		clearPassword()
	}

	func clearPassword() {
		password.text = ""
	}

	var body: some View {
		VStack(spacing: theme.itemSpacing) {
			theme.enterPasswordIcon
				.scaledToFit()
				.frame(width: theme.iconSizeLarge, height: theme.iconSizeLarge)

			Text(title)
				.textAppearance(theme.textAppearanceTitle)

			Text(info)
				.textAppearance(theme.textAppearanceBody)
				.multilineTextAlignment(.center)

			if !lastAttemptSuccessful {
				Text("The last input was incorrect!")
					.textAppearance(theme.textAppearanceBody)
			}

			PasswordTextField(placeholder: passwordPlaceholder, password: password, error: .constant(false))

			if [1, 2].contains(retryCounter) && self.passwordType == .pin {
				Text("\(retryCounter) PIN attempt(s) left.")
					.textAppearance(theme.textAppearanceBody)
			}

			Spacer()
			HStack {
				Spacer()
			}
		}
		.padding(theme.itemSpacing)
		.contentShape(Rectangle())
		.onTapGesture {
			hideKeyboard()
		}
		.navigationBarTitle(Text(navigationTitle))
		.navigationBarItems(leading: AbortButton(), trailing: Button(action: {
			self.acceptPassword()
		}, label: {
			Text("Continue")
				.textAppearance(theme.textAppearanceButton)
		}).disabled(!validPassword))
		.onAppear {
			self.clearPassword()
			self.password.maxLength = self.passwordLength
		}
	}
}
