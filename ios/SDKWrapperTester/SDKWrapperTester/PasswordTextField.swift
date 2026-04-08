/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

struct PasswordTextField: View {
	let placeholder: String
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	@ObservedObject var password: MaxLengthString
	@State private var hidden = true
	@Binding var error: Bool

	var body: some View {
		HStack {
			if hidden {
				SecureField(placeholder, text: $password.text)
					.keyboardType(.numberPad)
					.frame(height: 20)
			} else {
				TextField(placeholder, text: $password.text)
					.keyboardType(.numberPad)
					.frame(height: 20)
			}

			Button(action: {
				if !self.error {
					self.hidden.toggle()
				}
			}, label: {
				if self.error {
					theme.passwordErrorIcon
						.scaledToFit()
						.frame(width: theme.iconSizeSmall, height: theme.iconSizeSmall)
				} else {
					if self.hidden {
						theme.passwordHiddenIcon
							.scaledToFit()
							.frame(width: theme.iconSizeSmall, height: theme.iconSizeSmall)
					} else {
						theme.passwordVisibleIcon
							.scaledToFit()
							.frame(width: theme.iconSizeSmall, height: theme.iconSizeSmall)
					}
				}
			})
		}
		.padding()
		.background(ZStack {
			Capsule()
				.fill(Color("text_field_background"))
			if self.error {
				Capsule(style: .continuous)
					.stroke(Color.red, lineWidth: 2)
			}
		})
		.frame(width: 200)
	}
}
