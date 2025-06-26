// swift-tools-version:5.7
import PackageDescription

let package = Package(
	name: "AusweisApp2SDKWrapper",
	platforms: [
		.iOS(.v16)
	],
	products: [
		.library(
			name: "AusweisApp2SDKWrapper",
			type: .dynamic,
			targets: ["AusweisApp2SDKWrapper"]
		)
	],
	dependencies: [
		.package(
			name: "AusweisApp2",
			url: "https://github.com/Governikus/AusweisApp2-SDK-iOS",
			.exact("2.3.2")
		)
	],
	targets: [
		.target(
			name: "AusweisApp2SDKWrapper",
			dependencies: ["AusweisApp2"]
		),
		.testTarget(
			name: "SDKWrapperTests",
			dependencies: ["AusweisApp2SDKWrapper"]
		)
	]
)
