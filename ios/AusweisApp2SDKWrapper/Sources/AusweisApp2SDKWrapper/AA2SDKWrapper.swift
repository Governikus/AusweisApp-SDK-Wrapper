/**
 * Copyright (c) 2020-2025 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

@available(iOS 16, *)
public enum AA2SDKWrapper {
	public static let workflowController = WorkflowController(withConnection: AA2SdkConnection.shared)
}
