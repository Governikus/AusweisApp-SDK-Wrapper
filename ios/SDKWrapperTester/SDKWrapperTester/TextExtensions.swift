/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
 */

import Foundation
import SwiftUI

extension Text {
	func textAppearance(_ textAppearance: TextAppearance) -> Text {
		font(textAppearance.font)
			.foregroundColor(textAppearance.color)
	}
}
