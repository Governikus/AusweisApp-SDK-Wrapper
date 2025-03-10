/**
 * Copyright (c) 2020-2025 Governikus GmbH & Co. KG, Germany
 */

import Foundation

enum AA2Messages {
	static let msgAccessRights = "ACCESS_RIGHTS"
	static let msgAuth = "AUTH"
	static let msgCertificate = "CERTIFICATE"
	static let msgChangePin = "CHANGE_PIN"
	static let msgEnterPin = "ENTER_PIN"
	static let msgEnterNewPin = "ENTER_NEW_PIN"
	static let msgEnterPuk = "ENTER_PUK"
	static let msgEnterCan = "ENTER_CAN"
	static let msgInsertCard = "INSERT_CARD"
	static let msgBadState = "BAD_STATE"
	static let msgReader = "READER"
	static let msgInvalid = "INVALID"
	static let msgUnknowCommand = "UNKNOWN_COMMAND"
	static let msgInternalError = "INTERNAL_ERROR"
	static let msgStatus = "STATUS"
	static let msgInfo = "INFO"
	static let msgReaderList = "READER_LIST"
	static let msgPause = "PAUSE"
}

struct AA2Message: Decodable {
	let msg: String
	let error: String?
	let card: AA2Card?
	let result: AA2Result?
	let chat: AA2Chat?
	let aux: AA2Aux?
	let transactionInfo: String?
	let validity: AA2Validity?
	let description: AA2Description?
	let url: String?
	let success: Bool?
	let reason: String?
	let reader: AA2Reader?
	let readers: [AA2Reader]?
	let name: String?
	let insertable: Bool?
	let attached: Bool?
	let keypad: Bool?
	let workflow: String?
	let progress: Int?
	let state: String?
	let versionInfo: AA2VersionInfo?
	let cause: String?

	enum CodingKeys: String, CodingKey {
		case versionInfo = "VersionInfo"

		case msg, error, card, result, chat, aux, transactionInfo, validity
		case description, url, success, reader, workflow, progress, state
		case name, insertable, attached, keypad, readers, reason, cause
	}
}

struct AA2Description: Decodable {
	let issuerName: String
	let issuerUrl: String
	let purpose: String
	let subjectName: String
	let subjectUrl: String
	let termsOfUsage: String
}

struct AA2Validity: Decodable {
	let effectiveDate: String
	let expirationDate: String
}

struct AA2Chat: Decodable {
	let effective: [String]
	let optional: [String]
	let required: [String]
}

struct AA2Aux: Decodable {
	let ageVerificationDate: String?
	let requiredAge: String?
	let validityDate: String?
	let communityId: String?
}

struct AA2Card: Decodable {
	let deactivated: Bool?
	let inoperative: Bool?
	let retryCounter: Int?
}

struct AA2Reader: Decodable {
	let name: String
	let insertable: Bool
	let attached: Bool
	let keypad: Bool
	let card: AA2Card?
}

struct AA2Result: Decodable {
	let major: String?
	let minor: String?
	let url: String?
	let language: String?
	let description: String?
	let message: String?
	let reason: String?
}

struct AA2VersionInfo: Decodable {
	let name: String
	let implementationTitle: String
	let implementationVendor: String
	let implementationVersion: String
	let specificationTitle: String
	let specificationVendor: String
	let specificationVersion: String

	enum CodingKeys: String, CodingKey {
		case name = "Name"
		case implementationTitle = "Implementation-Title"
		case implementationVendor = "Implementation-Vendor"
		case implementationVersion = "Implementation-Version"
		case specificationTitle = "Specification-Title"
		case specificationVendor = "Specification-Vendor"
		case specificationVersion = "Specification-Version"
	}
}
