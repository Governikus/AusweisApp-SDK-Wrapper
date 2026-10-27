/*
 * Copyright (c) 2026 Governikus Service GmbH, Germany
 */

@testable import AusweisApp2SDKWrapper
import XCTest

@available(iOS 17.0, *)
class WorkflowControllerTests: XCTestCase {
	private var workflowController: WorkflowController!
	private var testCallbacks: TestWorkflowCallbacks!

	override func setUp() {
		workflowController = AA2SDKWrapper.workflowController
		testCallbacks = TestWorkflowCallbacks()
		workflowController.registerCallbacks(testCallbacks)
	}

	override func tearDown() {
		workflowController.unregisterCallbacks(testCallbacks)
		workflowController = nil
		testCallbacks = nil
	}

	func testChangePinWithSimulator() {
		let onStartedExpectation = expectation(description: "onStarted called")
		testCallbacks.doOnStarted = {
			onStartedExpectation.fulfill()
			self.workflowController.startChangePin()
		}

		let onChangePinStartedExpectation = expectation(description: "onChangePinStarted called")
		testCallbacks.doOnChangePinStarted = {
			onChangePinStartedExpectation.fulfill()
		}

		let onRequestCardExpectation = expectation(description: "onInsertCard called")
		testCallbacks.doOnRequestCard = {
			onRequestCardExpectation.fulfill()
			self.workflowController.setCard(name: "Simulator")
		}

		let onSetPinExpectation = expectation(description: "onSetPin called")
		testCallbacks.doOnRequestPin = { _ in
			onSetPinExpectation.fulfill()
			self.workflowController.setPin(nil)
		}

		let onSetNewPinExpectation = expectation(description: "onSetNewPin called")
		testCallbacks.doOnRequestNewPin = { _ in
			onSetNewPinExpectation.fulfill()
			self.workflowController.setNewPin(nil)
		}

		let onChangePinCompletedExpectation = expectation(description: "onChangePinCompleted called")
		testCallbacks.doOnChangePinCompleted = { changePinResult in
			onChangePinCompletedExpectation.fulfill()
			XCTAssertTrue(changePinResult.success)
		}

		workflowController.start()
		waitForExpectations(timeout: 5, handler: nil)
		workflowController.stop()
	}

	func testAuthWithSimulator() {
		let onStartedExpectation = expectation(description: "onStarted called")
		testCallbacks.doOnStarted = {
			onStartedExpectation.fulfill()
			// swiftlint:disable line_length
			self.workflowController.startAuthentication(withTcTokenUrl: URL(string: "https://test.governikus-eid.de/AusweisAuskunft/WebServiceRequesterServlet?mode=json")!)
			// swiftlint:enable line_length
		}

		let onAuthStartedExpectation = expectation(description: "onAuthenticationStarted called")
		testCallbacks.doOnAuthenticationStarted = {
			onAuthStartedExpectation.fulfill()
		}

		let onAccessRightsExpectation = expectation(description: "onRequestAccessRights called")
		testCallbacks.doOnRequestAccessRights = { _ in
			onAccessRightsExpectation.fulfill()
			self.workflowController.accept()
		}

		let onRequestCardExpectation = expectation(description: "onInsertCard called")
		testCallbacks.doOnRequestCard = {
			onRequestCardExpectation.fulfill()
			self.workflowController.setCard(name: "Simulator")
		}

		let onSetPinExpectation = expectation(description: "onSetPin called")
		testCallbacks.doOnRequestPin = { _ in
			onSetPinExpectation.fulfill()
			self.workflowController.setPin(nil)
		}

		let onAuthCompletedExpectation = expectation(description: "onAuthenticationCompleted called")
		testCallbacks.doOnAuthenticationCompleted = { authResult in
			onAuthCompletedExpectation.fulfill()
			XCTAssertNotNil(authResult.url)
			XCTAssertNotNil(authResult.result)
			XCTAssertEqual(authResult.result!.major, "http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok")
		}

		workflowController.start()
		waitForExpectations(timeout: 10, handler: nil)
		workflowController.stop()
	}
}
