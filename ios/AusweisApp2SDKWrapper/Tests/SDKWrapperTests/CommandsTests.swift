/*
 * Copyright (c) 2026 Governikus GmbH & Co. KG, Germany
 */

@testable import AusweisApp2SDKWrapper
import XCTest

class CommandsTests: XCTestCase {
	private let jsonEncoder = JSONEncoder()
	private let jsonDecoder = JSONDecoder()

	func XCTAssertSuccessReturn<T>(
		_ expression: @autoclosure () throws -> T,
		in file: StaticString = #file,
		line: UInt = #line
	) -> T {
		do {
			return try expression()
		} catch {
			XCTFail(
				error.localizedDescription,
				file: file,
				line: line
			)
			fatalError(error.localizedDescription)
		}
	}

	func encodeCommand(command: Command) -> Data {
		XCTAssertSuccessReturn(try jsonEncoder.encode(command))
	}

	// swiftlint:disable function_body_length line_length
	func testSerialization() {
		struct TestCase {
			let command: Command
			let expectedJSON: String
			let line: UInt
		}

		let datasets: [TestCase] = [
			TestCase(
				command: Accept(),
				expectedJSON: #"{"cmd":"ACCEPT"}"#,
				line: #line
			),
			TestCase(
				command: Cancel(),
				expectedJSON: #"{"cmd":"CANCEL"}"#,
				line: #line
			),
			TestCase(
				command: ContinueWorkflow(),
				expectedJSON: #"{"cmd":"CONTINUE"}"#,
				line: #line
			),
			TestCase(
				command: GetCertificate(),
				expectedJSON: #"{"cmd":"GET_CERTIFICATE"}"#,
				line: #line
			),
			TestCase(
				command: RunAuth(
					tcTokenURL: "https://example.org",
					developerMode: false,
					messages: nil,
					status: true,
					header: ["Bearer": "0123456789abcdef"]
				),
				expectedJSON: #"{"status":true,"tcTokenURL":"https://example.org","header":{"Bearer":"0123456789abcdef"},"cmd":"RUN_AUTH","developerMode":false}"#,
				line: #line
			),
			TestCase(
				command: RunChangePin(messages: nil, status: true),
				expectedJSON: #"{"status":true,"cmd":"RUN_CHANGE_PIN"}"#,
				line: #line
			),
			TestCase(
				command: SetAccessRights(chat: []),
				expectedJSON: #"{"cmd":"SET_ACCESS_RIGHTS","chat":[]}"#,
				line: #line
			),
			TestCase(
				command: GetAccessRights(),
				expectedJSON: #"{"cmd":"GET_ACCESS_RIGHTS"}"#,
				line: #line
			),
			TestCase(
				command: SetCan(value: "123456"),
				expectedJSON: #"{"cmd":"SET_CAN","value":"123456"}"#,
				line: #line
			),
			TestCase(
				command: SetCan(value: nil),
				expectedJSON: #"{"cmd":"SET_CAN"}"#,
				line: #line
			),
			TestCase(
				command: SetPin(value: "123456"),
				expectedJSON: #"{"cmd":"SET_PIN","value":"123456"}"#,
				line: #line
			),
			TestCase(
				command: SetPin(value: nil),
				expectedJSON: #"{"cmd":"SET_PIN"}"#,
				line: #line
			),
			TestCase(
				command: SetNewPin(value: "123456"),
				expectedJSON: #"{"cmd":"SET_NEW_PIN","value":"123456"}"#,
				line: #line
			),
			TestCase(
				command: SetNewPin(value: nil),
				expectedJSON: #"{"cmd":"SET_NEW_PIN"}"#,
				line: #line
			),
			TestCase(
				command: SetPuk(value: "1234567890"),
				expectedJSON: #"{"cmd":"SET_PUK","value":"1234567890"}"#,
				line: #line
			),
			TestCase(
				command: SetPuk(value: nil),
				expectedJSON: #"{"cmd":"SET_PUK"}"#,
				line: #line
			),
			TestCase(
				command: Interrupt(),
				expectedJSON: #"{"cmd":"INTERRUPT"}"#,
				line: #line
			),
			TestCase(
				command: GetStatus(),
				expectedJSON: #"{"cmd":"GET_STATUS"}"#,
				line: #line
			),
			TestCase(
				command: GetInfo(),
				expectedJSON: #"{"cmd":"GET_INFO"}"#,
				line: #line
			),
			TestCase(
				command: GetReader(name: "reader123"),
				expectedJSON: #"{"cmd":"GET_READER","name":"reader123"}"#,
				line: #line
			),
			TestCase(
				command: GetReaderList(),
				expectedJSON: #"{"cmd":"GET_READER_LIST"}"#,
				line: #line
			),
			TestCase(
				command: SetCard(name: "card123", simulator: nil),
				expectedJSON: #"{"cmd":"SET_CARD","name":"card123"}"#,
				line: #line
			)
		]

		for data in datasets {
			do {
				let actualData = encodeCommand(command: data.command)
				let expectedData = try XCTUnwrap(data.expectedJSON.data(using: .utf8))
				let actualDict = try JSONSerialization.jsonObject(with: actualData, options: []) as? [String: AnyHashable]
				let expectedDict = try JSONSerialization.jsonObject(with: expectedData, options: []) as? [String: AnyHashable]

				XCTAssertEqual(
					actualDict,
					expectedDict,
					"Failed serialization for \(type(of: data.command))",
					file: #file,
					line: data.line
				)
			} catch {
				fatalError("JSON formatting error: \(error.localizedDescription)")
			}
		}
	}
	// swiftlint:enable function_body_length line_length
}
