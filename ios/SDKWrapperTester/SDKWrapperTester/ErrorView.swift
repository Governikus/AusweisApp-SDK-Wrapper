/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

struct ErrorView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	@ObservedObject var viewModel: CardWorkflowViewModel

	var body: some View {
		VStack(spacing: theme.itemSpacing) {
			theme.errorIcon
				.scaledToFit()
				.frame(width: theme.iconSizeLarge, height: theme.iconSizeLarge)

			Text("An error occurred")
				.textAppearance(theme.textAppearanceTitle)
				.multilineTextAlignment(.center)

			if viewModel.currentErrorMessage != nil {
				Text(viewModel.currentErrorMessage!)
					.textAppearance(theme.textAppearanceBody)
					.multilineTextAlignment(.center)
			} else {
				Text("An unknown error occurred during authentication. Please inform the software manufacturer about the problem.")
					.textAppearance(theme.textAppearanceBody)
					.multilineTextAlignment(.center)
			}

			Spacer()
		}
		.padding(theme.itemSpacing)
		.navigationBarTitle(Text("Error"))
		.navigationBarItems(leading: EmptyView(), trailing: Button(action: {
			self.viewModel.acceptError()
		}, label: {
			if viewModel.state != .unknownCard {
				Text("Continue")
					.textAppearance(theme.textAppearanceButton)
			}
		}))
	}
}
