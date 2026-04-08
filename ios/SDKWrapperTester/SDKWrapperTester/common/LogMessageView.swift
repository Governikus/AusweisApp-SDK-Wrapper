/*
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

import SwiftUI

struct LogMessageView: View {
	let message: LogMessage

	var body: some View {
		Text(message.text)
			.contextMenu {
				Button(action: {
					UIPasteboard.general.string = message.text
				}, label: {
					Text("Copy")
					Image(systemName: "doc.on.doc")
				})
			}
	}
}

struct LogMessageView_Previews: PreviewProvider {
	static var previews: some View {
		LogMessageView(message: LogMessage(text: "Hello World!"))
	}
}
