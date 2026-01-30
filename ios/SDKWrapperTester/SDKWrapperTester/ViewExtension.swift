/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

extension View {
	func hideKeyboard() {
		UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
	}
}
