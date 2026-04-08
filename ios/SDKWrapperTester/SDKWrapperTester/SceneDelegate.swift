/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import SwiftUI
import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
	var window: UIWindow?

	func scene(
		_ scene: UIScene, willConnectTo _: UISceneSession,
		options _: UIScene.ConnectionOptions
	) {
		if let windowScene = scene as? UIWindowScene {
			let uiWindow = UIWindow(windowScene: windowScene)

			let hostingController = UIHostingController(rootView: AnyView(EmptyView()))
			hostingController.rootView = AnyView(ContentView(viewController: hostingController))
			uiWindow.rootViewController = hostingController
			window = uiWindow
			uiWindow.makeKeyAndVisible()
		}
	}
}
