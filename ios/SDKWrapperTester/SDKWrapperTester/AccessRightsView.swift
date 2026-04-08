/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

final class OptionalRight: ObservableObject {
	let right: AccessRight
	let display: String
	@Published var selected: Bool

	init(right: AccessRight, display: String?, selected: Bool) {
		self.right = right
		self.display = display ?? right.rawValue
		self.selected = selected
	}
}

struct OptionalRightToggle: View {
	@ObservedObject var optionalRight: OptionalRight
	@EnvironmentObject var theme: SDKWrapperTesterTheme

	var body: some View {
		Toggle(isOn: $optionalRight.selected, label: {
			Text(optionalRight.display)
				.textAppearance(theme.textAppearanceListItemCaption)
		})
	}
}

struct AccessRightsView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme

	var accessRights: AccessRights
	@Binding var certificateDescription: CertificateDescription?
	var onAcceptAccessRights: () -> Void
	var onSetAccessRights: (_ optionalRights: [AccessRight]) -> Void
	@State private var optionalRights: [OptionalRight]

	init(
		accessRights: AccessRights,
		certificateDescription: Binding<CertificateDescription?>,
		onAcceptAccessRights: @escaping () -> Void,
		onSetAccessRights: @escaping (_ optionalRights: [AccessRight]) -> Void
	) {
		self.accessRights = accessRights
		_certificateDescription = certificateDescription
		self.onAcceptAccessRights = onAcceptAccessRights
		self.onSetAccessRights = onSetAccessRights

		let foundOptionalRights = accessRights.optionalRights.map { right -> OptionalRight in
			let selected = accessRights.effectiveRights.contains(right)
			return OptionalRight(right: right, display: AccessRightNames.pretty[right] ?? right.rawValue, selected: selected)
		}
		_optionalRights = State(initialValue: foundOptionalRights)
	}

	var body: some View {
		Form {
			Section(header:
				Text("Certificate")
					.textAppearance(theme.textAppearanceListTitle))
			{ // swiftlint:disable:this opening_brace
				if certificateDescription != nil {
					HStack {
						Text("Provider")
							.textAppearance(theme.textAppearanceListItemCaption)
						Spacer()
						Text(certificateDescription!.subjectName)
							.textAppearance(theme.textAppearanceListItem)
					}
					HStack {
						Text("Purpose")
							.textAppearance(theme.textAppearanceListItemCaption)
						Spacer()
						Text(certificateDescription!.purpose)
							.foregroundColor(.secondary)
					}
					NavigationLink(
						destination: CertificateInformationView(description: certificateDescription!),
						label: { Text("Certificate details") }
					)
				} else {
					Text("Certificate description can't be displayed")
						.textAppearance(theme.textAppearanceListItemCaption)
				}
			}

			if !self.accessRights.requiredRights.isEmpty {
				Section(header:
					Text("Required Data")
						.textAppearance(theme.textAppearanceListTitle))
				{ // swiftlint:disable:this opening_brace
					ForEach(accessRights.requiredRights, id: \.self) { right in
						Text(AccessRightNames.pretty[right] ?? right.rawValue)
							.textAppearance(self.theme.textAppearanceListItemCaption)
					}
				}
			}

			if !self.optionalRights.isEmpty {
				Section(header:
					Text("Optional Data")
						.textAppearance(theme.textAppearanceListTitle))
				{ // swiftlint:disable:this opening_brace
					ForEach(self.optionalRights, id: \.self.display) { right in
						OptionalRightToggle(optionalRight: right)
					}
				}
			}
		}
		.navigationBarTitle(Text("Requested Data"))
		.navigationBarItems(leading: AbortButton(), trailing: Button(action: {
			if self.optionalRights.isEmpty {
				self.onAcceptAccessRights()
			} else {
				let selectedRights = self.optionalRights.filter { $0.selected }.map { $0.right }
				self.onSetAccessRights(selectedRights)
			}
		}, label: {
			Text("Accept")
				.textAppearance(theme.textAppearanceButton)
		}))
	}
}
