/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

extension Text {
	func textAppearance(_ textAppearance: TextAppearance) -> Text {
		return font(textAppearance.font)
			.foregroundColor(textAppearance.color)
	}
}
