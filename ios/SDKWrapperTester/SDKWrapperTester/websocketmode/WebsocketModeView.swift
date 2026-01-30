/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

import SwiftUI

struct WebsocketModeView: View {
	@ObservedObject var model = WebsocketModeModel()

	var body: some View {
		VStack {
			VStack {
				HStack {
					Button(action: {
						model.start()
					}, label: {
						Text("Start Websocket")
					}).disabled(model.isStarted)

					Button(action: {
						model.stop()
					}, label: {
						Text("Cancel")
					}).disabled(!model.isStarted)
				}
			}
			.padding()

			LogView(messages: $model.logMessages)
		}
		.navigationBarTitle("WebsocketMode")
		.onDisappear {
			model.stop()
		}
	}
}

struct WebsocketModeView_Previews: PreviewProvider {
	static var previews: some View {
		WebsocketModeView()
	}
}
