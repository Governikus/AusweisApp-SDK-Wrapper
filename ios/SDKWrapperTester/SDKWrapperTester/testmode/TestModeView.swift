/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

import SwiftUI

struct TestModeView: View {
	@ObservedObject var model = TestModeModel()
	@State var pin: String = "123456"
	@State var apiLevel: String = "3"
	@State var tcTokenUrl = "https://test.governikus-eid.de/AusweisAuskunft/WebServiceRequesterServlet"
	@State var useCustomMessages = true
	@State var handleInterrupt = true
	@State var interruptScan = false

	var body: some View {
		VStack {
			VStack {
				HStack {
					Text("tcTokenUrl:")
					TextField("Enter tcTokenUrl...", text: $tcTokenUrl)
						.keyboardType(.URL)
						.textFieldStyle(RoundedBorderTextFieldStyle())
						.disabled(model.isStarted)
				}

				HStack {
					Text("API Level:")
					TextField("Enter API Level...", text: $apiLevel)
						.keyboardType(.decimalPad)
						.textFieldStyle(RoundedBorderTextFieldStyle())
						.disabled(model.isStarted)
				}

				HStack {
					Text("PIN:")
					TextField("Enter PIN...", text: $pin)
						.keyboardType(.decimalPad)
						.textFieldStyle(RoundedBorderTextFieldStyle())
						.disabled(model.isStarted)

					Button(action: {
						hideKeyboard()
						model.authenticate(
							to: tcTokenUrl,
							withPin: pin,
							useCustomMessages: useCustomMessages,
							handleInterrupt: handleInterrupt,
							interruptScan: interruptScan,
							apiLevel: apiLevel
						)
					}, label: {
						Text("Authenticate")
					}).disabled(model.isStarted)

					Button(action: {
						model.stop()
					}, label: {
						Text("Cancel")
					}).disabled(!model.isStarted)
				}

				HStack {
					Toggle("Messages", isOn: $useCustomMessages)
						.disabled(model.isStarted)

					Toggle("Interrupt scan", isOn: $interruptScan)
						.disabled(model.isStarted || handleInterrupt)
				}

				HStack {
					Toggle("Handle interrupt", isOn: $handleInterrupt)
						.disabled(model.isStarted)

					Button(action: {
						model.setPin()
					}, label: {
						Text("Send PIN")
					}).disabled(!(model.isStarted && !handleInterrupt && interruptScan))
						.padding()
				}

				HStack {
					Button(action: {
						guard let redirectUrl = model.redirectUrl else { return }
						UIApplication.shared.open(redirectUrl)
					}, label: {
						Text("Open result URL")
					}).disabled(model.isStarted || model.redirectUrl == nil)
				}
			}
			.padding()

			LogView(messages: $model.logMessages)
		}
		.navigationBarTitle("TestMode")
		.onDisappear {
			model.stop()
		}
	}
}

struct TestModeView_Previews: PreviewProvider {
	static var previews: some View {
		TestModeView()
	}
}
