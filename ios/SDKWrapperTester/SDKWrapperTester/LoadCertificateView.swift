/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

struct LoadCertificateView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme

	var body: some View {
		VStack(spacing: theme.itemSpacing) {
			theme.loadCertificateIcon
				.scaledToFit()
				.frame(width: theme.iconSizeLarge, height: theme.iconSizeLarge)

			Text("Please wait until the service provider certificate has been loaded.")
				.textAppearance(theme.textAppearanceBody)
				.multilineTextAlignment(.center)

			ActivityIndicator(isAnimating: .constant(true), style: .large)
				.padding(theme.itemSpacing)

			Spacer()
		}
		.padding(theme.itemSpacing)
		.navigationBarTitle(Text("Loading certificate…"))
		.navigationBarItems(leading: AbortButton(), trailing: EmptyView())
	}
}
