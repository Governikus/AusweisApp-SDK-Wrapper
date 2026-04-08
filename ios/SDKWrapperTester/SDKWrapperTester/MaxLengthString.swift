/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation

class MaxLengthString: ObservableObject {
	@Published var text: String = "" {
		didSet {
			if text.count > maxLength, maxLength != -1 {
				text = String(text.prefix(maxLength))
			}
		}
	}

	@Published var maxLength: Int

	init(maxLength: Int = -1) {
		self.maxLength = maxLength
	}
}
