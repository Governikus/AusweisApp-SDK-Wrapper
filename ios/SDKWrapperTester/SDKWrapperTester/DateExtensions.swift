/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
 */

import Foundation

extension Date {
	func toString(format: String = "dd.MM.yyyy") -> String {
		let formatter = DateFormatter()
		formatter.dateFormat = format
		return formatter.string(from: self)
	}
}
