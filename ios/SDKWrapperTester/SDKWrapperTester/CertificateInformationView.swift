/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

struct CertificateInformationView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	@State var description: CertificateDescription

	var body: some View {
		Form {
			Section(header:
				Text("Issuer information")
					.textAppearance(theme.textAppearanceListTitle)
			) {
				HStack {
					Text("Issuer name")
						.textAppearance(theme.textAppearanceListItemCaption)
					Spacer()
					Text(description.issuerName)
						.textAppearance(theme.textAppearanceListItem)
				}
				HStack {
					Text("Issuer URL")
						.textAppearance(theme.textAppearanceListItemCaption)
					Spacer()
					Text(description.issuerUrl?.absoluteString ?? "")
						.textAppearance(theme.textAppearanceListItem)
				}
			}

			Section(header:
				Text("Subject information")
					.textAppearance(theme.textAppearanceListTitle)
			) {
				HStack {
					Text("Purpose")
						.textAppearance(theme.textAppearanceListItemCaption)
					Spacer()
					Text(description.purpose)
						.textAppearance(theme.textAppearanceListItem)
				}
				HStack {
					Text("Subject name")
						.textAppearance(theme.textAppearanceListItemCaption)
					Spacer()
					Text(description.subjectName)
						.textAppearance(theme.textAppearanceListItem)
				}
				HStack {
					Text("Subject URL")
						.textAppearance(theme.textAppearanceListItemCaption)
					Spacer()
					Text(description.subjectUrl?.absoluteString ?? "")
						.textAppearance(theme.textAppearanceListItem)
				}
			}

			Section(header:
				Text("Terms of Usage")
					.textAppearance(theme.textAppearanceListTitle)
			) {
				Text(description.termsOfUsage)
					.textAppearance(theme.textAppearanceListItemCaption)
			}

			Section(header:
				Text("Validity")
					.textAppearance(theme.textAppearanceListTitle)
			) {
				HStack {
					Text("Issue date")
						.textAppearance(theme.textAppearanceListItemCaption)
					Spacer()
					Text(description.validity.effectiveDate.toString())
						.textAppearance(theme.textAppearanceListItem)
				}
				HStack {
					Text("Expiration date")
						.textAppearance(theme.textAppearanceListItemCaption)
					Spacer()
					Text(description.validity.expirationDate.toString())
						.textAppearance(theme.textAppearanceListItem)
				}
			}
		}
		.navigationBarTitle(Text("Service provider details"))
	}
}
