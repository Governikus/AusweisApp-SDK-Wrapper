/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

// swiftlint:disable line_length

struct StartViewOnlineAuthentication: View {
	weak var viewController: UIViewController?
	@State private var tcTokenUrl
		= URL(string: "https://demo.governikus-eid.de/Autent-DemoApplication/RequestServlet?provider=demo_epa_20&redirect=true")!
	@State private var tcTokenUrlAllRightsRequired
		= URL(string: "https://test.governikus-eid.de/AusweisAuskunft/WebServiceRequesterServlet?mode=json")!
	@State private var tcTokenUrlCanAllowed
		= URL(string: "https://demo.governikus-eid.de/Autent-DemoApplication/RequestServlet?provider=demo_epa_can&redirect=true")!
	@State private var tcTokenUrlDeveloperMode
		= "http://demo.governikus-eid.de/Autent-DemoApplication/RequestServlet?provider=demo_epa_20&redirect=true"

	@State private var authResult: AuthResult?
	private let abortedMessage = "Workflow aborted"

	var body: some View {
		VStack(spacing: 20) {
			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.authenticate(withTcTokenUrl: self.tcTokenUrl, parentViewController: viewController, userInfoMessages: AA2UserInfoMessages(
					sessionStarted: "Session started",
					sessionFailed: "Session failed",
					sessionSucceeded: "Session succeeded",
					sessionInProgress: "Session in progress"
				)) { result in
					print(result ?? abortedMessage)
					self.authResult = result
				}
			}, label: {
				Text("Start Authentication")
			})

			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.authenticate(withTcTokenUrl: self.tcTokenUrl, parentViewController: viewController, simulatorMode: .defaultData) { result in
					print(result ?? abortedMessage)
					self.authResult = result
				}
			}, label: {
				Text("Start Authentication with Simulator")
			})

			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.authenticate(withTcTokenUrl: self.tcTokenUrl, parentViewController: viewController, simulatorMode: .differentFirstName) { result in
					print(result ?? abortedMessage)
					self.authResult = result
				}
			}, label: {
				Text("Start Authentication with Simulator (Different First Name)")
			})

			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.authenticate(withTcTokenUrl: self.tcTokenUrl, parentViewController: viewController, simulatorMode: .differentPseudonym) { result in
					print(result ?? abortedMessage)
					self.authResult = result
				}
			}, label: {
				Text("Start Authentication with Simulator (Different Pseudonym)")
			})

			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.authenticate(
					withTcTokenUrl: self.tcTokenUrlAllRightsRequired,
					parentViewController: viewController
				) { result in
					print(result ?? abortedMessage)
					self.authResult = result
				}
			}, label: {
				VStack {
					Text("Start Authentication")
					Text("(All access rights required)")
				}
			})

			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.authenticate(
					withTcTokenUrl: self.tcTokenUrlCanAllowed,
					parentViewController: viewController
				) { result in
					print(result ?? abortedMessage)
					self.authResult = result
				}
			}, label: {
				Text("Start CAN-allowed Authentication")
			})

			Text(
				"TcTokenURL for developerMode"
			)

			TextField(
				"TcTokenURL for developerMode",
				text: $tcTokenUrlDeveloperMode
			).textFieldStyle(.roundedBorder)

			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.authenticate(
					withTcTokenUrl: URL(string: self.tcTokenUrlDeveloperMode)!,
					parentViewController: viewController,
					developerMode: true
				) { result in
					print(result ?? abortedMessage)
					self.authResult = result
				}
			}, label: {
				VStack {
					Text("Start Authentication with developerMode")
				}
			})

			Spacer()

			if self.authResult?.result != nil || self.authResult?.url != nil {
				VStack(spacing: 20) {
					Text("Authentication result")
						.bold()

					if self.authResult?.result != nil && self.authResult?.hasError ?? true {
						Text("Error message:")
							.bold()
						Text(self.authResult!.result!.message ?? "Missing error message")
					}

					if self.authResult?.url != nil {
						Button(action: {
							guard let resultUrl = self.authResult?.url else { return }
							UIApplication.shared.open(resultUrl)
						}, label: {
							Text("Open result URL")
						})
					}
				}
				.padding(20)
				.border(self.authResult?.hasError ?? false ? Color.red : Color.green, width: 4)
			}
		}
		.padding(20)
		.navigationBarTitle(Text("Tester App - Online Auth"), displayMode: .inline)
	}
}

// swiftlint:enable line_length
