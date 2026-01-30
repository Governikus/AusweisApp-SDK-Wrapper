/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

import SwiftUI

struct LogMessage {
	let text: String
}

struct LogView: View {
	@Binding var messages: [LogMessage]

	var body: some View {
		List(messages, id: \.text) { message in
			LogMessageView(message: message)
		}
	}
}

struct LogView_Previews: PreviewProvider {
	static var previews: some View {
		LogView(messages: .constant([LogMessage(text: "Hello World")]))
	}
}
