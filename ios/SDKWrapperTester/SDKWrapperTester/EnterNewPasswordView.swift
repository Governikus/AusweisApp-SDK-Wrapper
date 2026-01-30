/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

struct EnterNewPasswordView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	@EnvironmentObject var viewModel: CardWorkflowViewModel

	@ObservedObject private var newPassword = MaxLengthString(maxLength: WorkflowController.pinLength)
	@ObservedObject private var newPasswordConfirmation = MaxLengthString(maxLength: WorkflowController.pinLength)

	var validPassword: Bool {
		newPassword.text == newPasswordConfirmation.text && newPassword.text.count == WorkflowController.pinLength
	}

	var confirmationMismatch: Bool {
		!validPassword && newPasswordConfirmation.text.count == WorkflowController.pinLength
	}

	var body: some View {
		let confirmationMismatchBinding = Binding(
			get: { self.confirmationMismatch },
			set: { _ = $0 }
		)

		return VStack(spacing: theme.itemSpacing) {
			theme.enterNewPasswordIcon
				.scaledToFit()
				.frame(width: theme.iconSizeLarge, height: theme.iconSizeLarge)

			Text("Change the PIN")
				.textAppearance(theme.textAppearanceTitle)

			Text("Please enter a new PIN and confirm it.")
				.textAppearance(theme.textAppearanceBody)
				.multilineTextAlignment(.center)

			PasswordTextField(placeholder: NSLocalizedString("New PIN",
			                                                 comment: "Placeholder for new PIN"),
			                  password: newPassword,
			                  error: .constant(false))
			VStack {
				PasswordTextField(placeholder: NSLocalizedString("Confirm PIN",
				                                                 comment: "Placeholder for confirmation of new PIN"),
				                  password: newPasswordConfirmation,
				                  error: confirmationMismatchBinding)

				if confirmationMismatch {
					Text("Confirmation PIN does not match new PIN.")
						.textAppearance(theme.textAppearanceWarning)
						.multilineTextAlignment(.center)
				}
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
		.navigationBarTitle(Text("Change PIN"))
		.navigationBarItems(leading: AbortButton(), trailing: Button(action: {
			self.viewModel.setNewPin(newPin: self.newPassword.text)
		}, label: {
			Text("Continue")
				.textAppearance(theme.textAppearanceButton)
		}).disabled(!validPassword))
	}
}
