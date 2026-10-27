/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
 */

@testable import AusweisApp2SDKWrapper
import XCTest

// swiftlint:disable type_body_length
class WorkflowControllerTestsMocked: XCTestCase {
	private var connection: MockSdkConnection!
	private var workflowController: WorkflowController!
	private var testCallbacks: TestWorkflowCallbacks!
	private var testUrl = URL(string: "https://test.test")!

	override func setUp() {
		connection = MockSdkConnection()
		workflowController = WorkflowController(withConnection: connection)
		testCallbacks = TestWorkflowCallbacks()
		workflowController.registerCallbacks(testCallbacks)
	}

	override func tearDown() {
		workflowController.unregisterCallbacks(testCallbacks)
		connection = nil
		workflowController = nil
		testCallbacks = nil
	}

	func testOnStarted() {
		let onStartedExpectation = expectation(description: "onStarted called")

		testCallbacks.doOnStarted = {
			onStartedExpectation.fulfill()
		}

		workflowController.start()

		waitForExpectations(timeout: 1, handler: nil)
		XCTAssertTrue(workflowController.isStarted)
	}

	func testErrorNotStarted() {
		let onErrorExpectation = expectation(description: "onWrapperError called")

		testCallbacks.doOnError = {
			onErrorExpectation.fulfill()
		}

		XCTAssertFalse(workflowController.isStarted)
		workflowController.startAuthentication(withTcTokenUrl: testUrl)

		waitForExpectations(timeout: 1, handler: nil)
	}

	func testAuthenticationStarted() {
		let onAuthenticationStartedExpectation = expectation(description: "onAuthenticationStarted called")
		let authCommandReceivedExpectation = expectation(description: "authCommandReceived")

		connection.onCommandSend = { command in
			let command = command as? RunAuth

			XCTAssertNotNil(command)
			XCTAssertEqual(self.testUrl.absoluteString, command!.tcTokenURL)

			authCommandReceivedExpectation.fulfill()
			self.connection.receive(messageJson: "{\"msg\":\"AUTH\"}")
		}

		testCallbacks.doOnAuthenticationStarted = {
			onAuthenticationStartedExpectation.fulfill()
		}

		workflowController.start()
		workflowController.startAuthentication(withTcTokenUrl: testUrl)

		waitForExpectations(timeout: 1, handler: nil)
	}

	// swiftlint:disable function_body_length
	func testFullAuthentication() {
		let pin = "123456"

		let exampleDateString = "1999-07-20"
		let exampleDate = exampleDateString.parseDate(format: "yyyy-MM-dd")

		let readerMessage = "{" +
			"  \"msg\": \"READER\"," +
			"  \"name\": \"NFC\"," +
			"  \"attached\": true," +
			"  \"insertable\": true," +
			"  \"keypad\": false," +
			"  \"card\":" +
			"         {" +
			"          \"inoperative\": false," +
			"          \"deactivated\": false," +
			"          \"retryCounter\": 3" +
			"         }" +
			"}"

		let authCommandReceivedExpectation = expectation(description: "authCommandReceived")
		let acceptReceivedExpectation = expectation(description: "acceptReceived")
		let setPinReceivedExpectation = expectation(description: "setPinReceived")
		connection.onCommandSend = { command in
			if let authCommand = command as? RunAuth {
				authCommandReceivedExpectation.fulfill()

				XCTAssertEqual(authCommand.tcTokenURL, self.testUrl.absoluteString)

				self.connection.receive(messageJson: "{\"msg\":\"AUTH\"}")
			} else if command is Accept {
				acceptReceivedExpectation.fulfill()
				self.connection.receive(messageJson: "{\"msg\":\"INSERT_CARD\"}")
			} else if let setPinCommand = command as? SetPin {
				setPinReceivedExpectation.fulfill()

				XCTAssertEqual(setPinCommand.value, pin)

				// swiftlint:disable line_length
				self.connection.receive(messageJson:
					"{" +
						"  \"msg\": \"AUTH\"," +
						"  \"result\":" +
						"           {" +
						"            \"major\": \"http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok\"" +
						"           }," +
						"  \"url\": \"https://test.governikus-eid.de/Autent-DemoApplication/refresh-address?sessionId=123456789&ResultMajor=ok\"" +
						"}")
				// swiftlint:enable line_length
			}
		}

		let onAuthenticationCompletedExpectation = expectation(description: "onAuthenticationCompleted called")
		testCallbacks.doOnAuthenticationCompleted = { _ in
			onAuthenticationCompletedExpectation.fulfill()
		}

		let onEnterPinExpectation = expectation(description: "onEnterPin called")
		testCallbacks.doOnRequestPin = { card in
			onEnterPinExpectation.fulfill()

			XCTAssertEqual(card.pinRetryCounter, 3)
			XCTAssertEqual(card.deactivated, false)
			XCTAssertEqual(card.inoperative, false)

			self.workflowController.setPin(pin)
		}

		let onRecognizedCardExpectation = expectation(description: "onRecognizedCard called")
		testCallbacks.doOnRecognizedCard = { card in
			onRecognizedCardExpectation.fulfill()

			XCTAssertNotNil(card)
			XCTAssertEqual(card?.pinRetryCounter, 3)
			XCTAssertEqual(card?.deactivated, false)
			XCTAssertEqual(card?.inoperative, false)

			self.connection.receive(messageJson:
				"{" +
					"  \"msg\": \"ENTER_PIN\"," +
					"  \"reader\":" +
					readerMessage +
					"}")
		}

		let onInsertCardExpectation = expectation(description: "onInsertCard called")
		testCallbacks.doOnRequestCard = {
			onInsertCardExpectation.fulfill()

			self.connection.receive(messageJson: readerMessage)
		}

		let onAccessRightsExpectation = expectation(description: "onAccessRights called")
		testCallbacks.doOnRequestAccessRights = { accessRights in
			onAccessRightsExpectation.fulfill()

			XCTAssertEqual(accessRights.auxiliaryData?.ageVerificationDate, exampleDate)
			XCTAssertEqual(accessRights.auxiliaryData?.requiredAge, 18)
			XCTAssertEqual(accessRights.auxiliaryData?.validityDate, exampleDate)
			XCTAssertEqual(accessRights.auxiliaryData?.communityId, "02760400110000")

			XCTAssertTrue(accessRights.requiredRights.contains { $0 == .Address })
			XCTAssertTrue(accessRights.requiredRights.contains { $0 == .FamilyName })
			XCTAssertTrue(accessRights.optionalRights.contains { $0 == .GivenNames })
			XCTAssertTrue(accessRights.optionalRights.contains { $0 == .AgeVerification })

			XCTAssertEqual(accessRights.transactionInfo, "this is an example")

			self.workflowController.accept()
		}

		let onAuthenticationStartedExpectation = expectation(description: "onAuthenticationStarted called")
		testCallbacks.doOnAuthenticationStarted = {
			onAuthenticationStartedExpectation.fulfill()

			self.connection.receive(messageJson:
				"{" +
					"  \"msg\": \"ACCESS_RIGHTS\"," +
					"  \"aux\":" +
					"       {" +
					"        \"ageVerificationDate\": \"" + exampleDateString + "\"," +
					"        \"requiredAge\": \"18\"," +
					"        \"validityDate\": \"" + exampleDateString + "\"," +
					"        \"communityId\": \"02760400110000\"" +
					"       }," +
					"  \"chat\":" +
					"        {" +
					"         \"effective\": [\"Address\", \"FamilyName\", \"GivenNames\", \"AgeVerification\"]," +
					"         \"optional\": [\"GivenNames\", \"AgeVerification\"]," +
					"         \"required\": [\"Address\", \"FamilyName\"]" +
					"        }," +
					"  \"transactionInfo\": \"this is an example\"" +
					"}")
		}

		workflowController.start()
		workflowController.startAuthentication(withTcTokenUrl: testUrl, withCustomHeader: ["Bearer": "0123456789abcdef"])
		waitForExpectations(timeout: 2, handler: nil)
	}

	func testChangePin() {
		let testPin = "123456"
		let newTestPin = "654321"

		let changePinCommandReceivedExpectation = expectation(description: "changePinCommandReceived")
		let setPinReceivedExpectation = expectation(description: "setPinReceived")
		let setNewPinReceivedExpectation = expectation(description: "setNewPinReceived")
		connection.onCommandSend = { command in
			if command is RunChangePin {
				changePinCommandReceivedExpectation.fulfill()
				self.connection.receive(messageJson: "{\"msg\":\"CHANGE_PIN\"}")
			} else if let setPinCommand = command as? SetPin {
				setPinReceivedExpectation.fulfill()
				XCTAssertEqual(setPinCommand.value, testPin)
				self.connection.receive(messageJson:
					"{" +
						"  \"msg\": \"ENTER_NEW_PIN\"," +
						"  \"reader\":" +
						"{" +
						"  \"msg\": \"READER\"," +
						"  \"name\": \"NFC\"," +
						"  \"attached\": true," +
						"  \"insertable\": true," +
						"  \"keypad\": false," +
						"  \"card\":" +
						"         {" +
						"          \"inoperative\": false," +
						"          \"deactivated\": false," +
						"          \"retryCounter\": 3" +
						"         }" +
						"}" +
						"}")
			} else if command is SetNewPin {
				setNewPinReceivedExpectation.fulfill()
				self.connection.receive(messageJson:
					"{" +
						"  \"msg\": \"CHANGE_PIN\"," +
						"  \"success\": true" +
						"}")
			}
		}

		let onChangePinStartedExpectation = expectation(description: "onChangePinStarted called")
		testCallbacks.doOnChangePinStarted = {
			onChangePinStartedExpectation.fulfill()
			self.connection.receive(messageJson: "{\"msg\":\"INSERT_CARD\"}")
		}

		let onInsertCardExpectation = expectation(description: "onInsertCard called")
		testCallbacks.doOnRequestCard = {
			onInsertCardExpectation.fulfill()
			self.connection.receive(messageJson:
				"{" +
					"  \"msg\": \"ENTER_PIN\"," +
					"  \"reader\":" +
					"{" +
					"  \"msg\": \"READER\"," +
					"  \"name\": \"NFC\"," +
					"  \"attached\": true," +
					"  \"insertable\": true," +
					"  \"keypad\": false," +
					"  \"card\":" +
					"         {" +
					"          \"inoperative\": false," +
					"          \"deactivated\": false," +
					"          \"retryCounter\": 3" +
					"         }" +
					"}" +
					"}")
		}

		let onEnterPinExpectation = expectation(description: "onEnterPin called")
		testCallbacks.doOnRequestPin = { card in
			onEnterPinExpectation.fulfill()

			XCTAssertEqual(card.pinRetryCounter, 3)
			XCTAssertEqual(card.deactivated, false)
			XCTAssertEqual(card.inoperative, false)

			self.workflowController.setPin(testPin)
		}

		let onEnterNewPinExpectation = expectation(description: "onEnterNewPin called")
		testCallbacks.doOnRequestNewPin = { card in
			onEnterNewPinExpectation.fulfill()

			XCTAssertEqual(card.pinRetryCounter, 3)
			XCTAssertEqual(card.deactivated, false)
			XCTAssertEqual(card.inoperative, false)

			self.workflowController.setNewPin(newTestPin)
		}

		let onChangePinCompletedExpectation = expectation(description: "onChangePinCompleted called")
		testCallbacks.doOnChangePinCompleted = { changePinResult in
			onChangePinCompletedExpectation.fulfill()
			XCTAssertTrue(changePinResult.success)
		}

		workflowController.start()
		workflowController.startChangePin()
		waitForExpectations(timeout: 2, handler: nil)
	}
}

// swiftlint:enable function_body_length
// swiftlint:enable type_body_length

class MockSdkConnection: SdkConnection {
	var isStarted = false

	var onConnected: (() -> Void)?
	var onMessageReceived: ((AA2Message) -> Void)?

	var onCommandSend: ((Command) -> Void)?

	func start() {
		isStarted = true
		if let onConnected {
			onConnected()
		}
	}

	func stop() {
		isStarted = false
	}

	func send(command: some Command) {
		if let onCommandSend {
			DispatchQueue.global().async {
				onCommandSend(command)
			}
		}
	}

	func receive(messageJson: String) {
		let messageData = Data(messageJson.utf8)
		let message = try? JSONDecoder().decode(AA2Message.self, from: messageData)

		if let onMessageReceived {
			onMessageReceived(message!)
		}
	}
}
