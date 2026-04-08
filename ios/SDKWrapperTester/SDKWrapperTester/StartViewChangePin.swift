/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

struct StartViewChangePin: View {
	weak var viewController: UIViewController?
	@State private var changePinResult: ChangePinResult?

	var body: some View {
		VStack(spacing: 20) {
			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.changePin(
					parentViewController: viewController,
					userInfoMessages: AA2UserInfoMessages(
						sessionStarted: "Session started",
						sessionFailed: "Session failed",
						sessionSucceeded: "Session succeeded",
						sessionInProgress: "Session in progress"
					)
				) { result in
					self.changePinResult = result
				}
			}, label: {
				Text("Change PIN")
			})

			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.changeTransportPin(parentViewController: viewController) { result in
					self.changePinResult = result
				}
			}, label: {
				Text("Change Transport PIN")
			})

			Button(action: {
				guard let viewController = self.viewController else { return }
				SDKWrapperTesterSDK.changePin(parentViewController: viewController, simulatorMode: .defaultData) { result in
					self.changePinResult = result
				}
			}, label: {
				Text("Change PIN with Simulator")
			})

			Spacer()

			if self.changePinResult != nil {
				VStack(spacing: 20) {
					Text("Change PIN result")
						.bold()

					if self.changePinResult?.success == true {
						Text("PIN successfully changed.")
					} else {
						Text("PIN change failed or aborted!")
						Text("Reason: \(self.changePinResult?.reason ?? "Missing Reason")")
					}
				}
				.padding(20)
				.border(self.changePinResult!.success ? Color.green : Color.red, width: 4)
			}
		}
		.padding(20)
		.navigationBarTitle(Text("Tester App - Change PIN"), displayMode: .inline)
	}
}
