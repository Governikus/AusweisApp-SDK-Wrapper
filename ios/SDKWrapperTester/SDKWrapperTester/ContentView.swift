/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import SwiftUI

struct ContentView: View {
	weak var viewController: UIViewController?

	var body: some View {
		NavigationView {
			ScrollView {
				VStack(spacing: 20) {
					Text("SDK Wrapper")
						.font(/*@START_MENU_TOKEN@*/ .title/*@END_MENU_TOKEN@*/)
						.fontWeight(/*@START_MENU_TOKEN@*/ .bold/*@END_MENU_TOKEN@*/)

					NavigationLink(
						destination: StartViewOnlineAuthentication(viewController: viewController),
						label: { Text("Online Authentication") }
					)

					NavigationLink(
						destination: StartViewChangePin(viewController: viewController),
						label: { Text("Change PIN") }
					)

					Text("SDK")
						.font(/*@START_MENU_TOKEN@*/ .title/*@END_MENU_TOKEN@*/)
						.fontWeight(/*@START_MENU_TOKEN@*/ .bold/*@END_MENU_TOKEN@*/)

					Text("In TestMode, a button can be used to test authentication.")
						.multilineTextAlignment(.center)
						.padding()
					NavigationLink(destination: TestModeView()) {
						Text("TestMode\n(uses integrated SDK)")
							.multilineTextAlignment(.center)
					}
					Text("""
					In websocket mode a websocket is opened which forwards all requests to the SDK.
					The IP of the websocket is written to the log window.
					""")
					.multilineTextAlignment(.center)
					.padding()
					NavigationLink(destination: WebsocketModeView()) {
						Text("WebsocketMode\n(uses integrated SDK)")
							.multilineTextAlignment(.center)
					}

					Spacer()
				}
				.padding(20)
				.navigationBarTitle(Text("Tester App - Start"))
			}
		}
	}
}
