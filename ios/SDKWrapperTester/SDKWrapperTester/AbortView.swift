/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

struct AbortView: View {
	@EnvironmentObject var viewModel: CardWorkflowViewModel
	@EnvironmentObject var theme: SDKWrapperTesterTheme

	var body: some View {
		VStack(spacing: theme.itemSpacing) {
			theme.authenticationAbortedIcon
				.scaledToFit()
				.frame(width: theme.iconSizeLarge, height: theme.iconSizeLarge)

			Text("The authentication was aborted, waiting for response from service provider.")
				.textAppearance(theme.textAppearanceBody)
				.multilineTextAlignment(.center)

			ActivityIndicator(isAnimating: .constant(true), style: .large)
				.padding(theme.itemSpacing)
			Spacer()
		}
		.padding(theme.itemSpacing)
		.navigationBarTitle(Text("Authentication aborted"))
		.navigationBarItems(leading: EmptyView(), trailing: EmptyView())
	}
}
