/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

struct CardRequestedView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	@ObservedObject var viewModel: CardWorkflowViewModel

	var body: some View {
		VStack(spacing: theme.itemSpacing) {
			let iconSize = (UIScreen.main.bounds.height > 700 ? theme.iconSizeLarge : theme.iconSizeSmall)
			theme.cardRequestedIcon
				.scaledToFit()
				.frame(width: iconSize, height: iconSize)

			ProgressView(value: Float(viewModel.workflowProgress.progress ?? 0), total: Float(100))
				.padding(theme.itemSpacing)

			Text("Communication with the ID card required, please place the device on the ID card.")
				.textAppearance(theme.textAppearanceBody)
				.multilineTextAlignment(.center)

			Spacer()
		}
		.padding(theme.itemSpacing)
		.navigationBarTitle(Text("Card access required"))
		.navigationBarItems(leading: AbortButton(), trailing: EmptyView())
	}
}
