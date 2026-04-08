/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

struct CardWorkflowView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	@EnvironmentObject var viewModel: CardWorkflowViewModel

	var body: some View {
		NavigationView {
			Group {
				if viewModel.state == .loadCertificate {
					LoadCertificateView()
				} else if viewModel.state == .abort {
					AbortView()
				} else if viewModel.state == .accessRights {
					if self.viewModel.accessRights != nil {
						AccessRightsView(
							accessRights: self.viewModel.accessRights!,
							certificateDescription: self.$viewModel.certificateDescription,
							onAcceptAccessRights: self.viewModel.acceptAccessRights,
							onSetAccessRights: self.viewModel.onSetAccessRights
						)
					} else {
						Text("Access rights could not be loaded")
							.textAppearance(theme.textAppearanceBody)
					}
				} else if viewModel.state == .enterPin {
					EnterPasswordView(passwordType: .pin)
				} else if viewModel.state == .enterTransportPin {
					EnterPasswordView(passwordType: .transportPin)
				} else if viewModel.state == .enterCan {
					EnterPasswordView(passwordType: .can)
				} else if viewModel.state == .enterPuk {
					EnterPasswordView(passwordType: .puk)
				} else if viewModel.state == .changePin {
					EnterNewPasswordView()
				} else if viewModel.state == .error ||
					viewModel.state == .unknownCard ||
					viewModel.state == .pause
				{ // swiftlint:disable:this opening_brace
					ErrorView(viewModel: self.viewModel)
				} else if viewModel.state == .cardRequested {
					CardRequestedView(viewModel: self.viewModel)
				} else {
					DummyView(state: self.viewModel.state)
				}
			}
		}
		.navigationViewStyle(StackNavigationViewStyle())
	}
}

struct DummyView: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	let state: ViewState

	var body: some View {
		VStack {
			Text("viewModel.state = \(String(describing: self.state))")
				.textAppearance(theme.textAppearanceBody)
			Spacer()
		}
		.navigationBarTitle(Text("Unknown state"))
	}
}

struct AbortButton: View {
	@EnvironmentObject var theme: SDKWrapperTesterTheme
	@EnvironmentObject var viewModel: CardWorkflowViewModel

	var body: some View {
		Button(action: { self.viewModel.cancelWorkflow() }, label: {
			Text("Abort")
				.textAppearance(theme.textAppearanceButton)
		})
	}
}
