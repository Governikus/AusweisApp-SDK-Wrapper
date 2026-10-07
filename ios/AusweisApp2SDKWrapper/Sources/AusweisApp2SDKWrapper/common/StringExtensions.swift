/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
 */

import Foundation

extension String {
	func parseDate(format: String) -> Date? {
		let dateFormatter = DateFormatter()
		dateFormatter.dateFormat = format
		return dateFormatter.date(from: self)
	}
}
