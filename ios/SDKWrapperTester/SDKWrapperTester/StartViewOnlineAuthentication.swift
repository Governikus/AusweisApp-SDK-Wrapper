/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

// swiftlint:disable line_length

struct StartViewOnlineAuthentication: View {
	weak var viewController: UIViewController?
	@State private var tcTokenUrl
		= URL(string: "https://test.governikus-eid.de/Autent-DemoApplication/api/eid/request")!
	@State private var tcTokenUrlAllRightsRequired
		= URL(string: "https://test.governikus-eid.de/Autent-DemoApplication/api/eid/request")!
	@State private var tcTokenUrlCanAllowed
		= URL(string: "https://test.governikus-eid.de/Vorort-DemoApplication/api/eid/request")!
	@State private var tcTokenUrlDeveloperMode
		= "http://test.governikus-eid.de/Autent-DemoApplication/api/eid/request"

	@State private var authResult: AuthResult?
	private let abortedMessage = "Workflow aborted"

	var body: some View {
		VStack(spacing: 20) {
			Button(action: {
				guard let viewController else { return }
				SDKWrapperTesterSDK.authenticate(withTcTokenUrl: tcTokenUrl, parentViewController: viewController, userInfoMessages: AA2UserInfoMessages(
					sessionStarted: "Session started\nPlease present the eID card",
					sessionFailed: "Session failed",
					sessionSucceeded: "Session succeeded",
					sessionInProgress: "Session in progress"
				),
				header: ["Bearer": "0123456789abcdef"]) { result in
					print(result ?? abortedMessage)
					authResult = result
				}
			}, label: {
				Text("Start Authentication")
			})

			Button(action: {
				guard let viewController else { return }
				SDKWrapperTesterSDK.authenticate(withTcTokenUrl: tcTokenUrl, parentViewController: viewController, simulatorMode: .defaultData, header: ["Bearer": "0123456789abcdef"]) { result in
					print(result ?? abortedMessage)
					authResult = result
				}
			}, label: {
				Text("Start Authentication with Simulator")
			})

			Button(action: {
				guard let viewController else { return }
				SDKWrapperTesterSDK.authenticate(withTcTokenUrl: tcTokenUrl, parentViewController: viewController, simulatorMode: .differentFirstName, header: ["Bearer": "0123456789abcdef"]) { result in
					print(result ?? abortedMessage)
					authResult = result
				}
			}, label: {
				Text("Start Authentication with Simulator (Different First Name)")
			})

			Button(action: {
				guard let viewController else { return }
				SDKWrapperTesterSDK.authenticate(withTcTokenUrl: tcTokenUrl, parentViewController: viewController, simulatorMode: .differentPseudonym, header: ["Bearer": "0123456789abcdef"]) { result in
					print(result ?? abortedMessage)
					authResult = result
				}
			}, label: {
				Text("Start Authentication with Simulator (Different Pseudonym)")
			})

			Button(action: {
				guard let viewController else { return }
				SDKWrapperTesterSDK.authenticate(
					withTcTokenUrl: tcTokenUrlAllRightsRequired,
					parentViewController: viewController,
					header: ["Bearer": "0123456789abcdef"]
				) { result in
					print(result ?? abortedMessage)
					authResult = result
				}
			}, label: {
				VStack {
					Text("Start Authentication")
					Text("(All access rights required)")
				}
			})

			Button(action: {
				guard let viewController else { return }
				SDKWrapperTesterSDK.authenticate(
					withTcTokenUrl: tcTokenUrlCanAllowed,
					parentViewController: viewController,
					header: ["Bearer": "0123456789abcdef"]
				) { result in
					print(result ?? abortedMessage)
					authResult = result
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
				guard let viewController else { return }
				SDKWrapperTesterSDK.authenticate(
					withTcTokenUrl: URL(string: tcTokenUrlDeveloperMode)!,
					parentViewController: viewController,
					developerMode: true,
					header: ["Bearer": "0123456789abcdef"]
				) { result in
					print(result ?? abortedMessage)
					authResult = result
				}
			}, label: {
				VStack {
					Text("Start Authentication with developerMode")
				}
			})

			Spacer()

			if authResult?.result != nil || authResult?.url != nil {
				VStack(spacing: 20) {
					Text("Authentication result")
						.bold()

					if authResult?.result != nil && authResult?.hasError ?? true {
						Text("Error message:")
							.bold()
						Text(authResult!.result!.message ?? "Missing error message")
					}

					if authResult?.url != nil {
						Button(action: {
							guard let resultUrl = authResult?.url else { return }
							UIApplication.shared.open(resultUrl)
						}, label: {
							Text("Open result URL")
						})
					}
				}
				.padding(20)
				.border(authResult?.hasError ?? false ? Color.red : Color.green, width: 4)
			}
		}
		.padding(20)
		.navigationBarTitle(Text("Tester App - Online Auth"), displayMode: .inline)
	}
}

// swiftlint:enable line_length
