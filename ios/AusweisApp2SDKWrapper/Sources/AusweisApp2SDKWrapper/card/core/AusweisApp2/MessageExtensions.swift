/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation

extension AA2Message {
	var aa2DateFormat: String {
		"yyyy-MM-dd"
	}

	func getCertificateDescription() -> CertificateDescription? {
		guard
			let description,
			let validity,
			let effectiveDate = validity.effectiveDate.parseDate(format: aa2DateFormat),
			let expirationDate = validity.expirationDate.parseDate(format: aa2DateFormat)
		else { return nil }

		return CertificateDescription(
			issuerName: description.issuerName,
			issuerUrl: URL(string: description.issuerUrl),
			purpose: description.purpose,
			subjectName: description.subjectName,
			subjectUrl: URL(string: description.subjectUrl),
			termsOfUsage: description.termsOfUsage,
			validity: CertificateValidity(
				effectiveDate: effectiveDate,
				expirationDate: expirationDate
			)
		)
	}

	func getReaders() -> [Reader]? {
		guard let readers else { return nil }

		return readers.compactMap { reader -> Reader? in Reader(reader: reader)
		}
	}

	func getReader() -> Reader? {
		if let reader {
			return Reader(reader: reader)
		}

		guard let name else { return nil }
		guard let insertable else { return nil }
		guard let attached else { return nil }
		guard let keypad else { return nil }

		return Reader(
			name: name,
			insertable: insertable,
			attached: attached,
			keypad: keypad,
			card: getCard()
		)
	}

	func getCard() -> Card? {
		guard let card = card ?? reader?.card else { return nil }

		return Card(card: card)
	}

	func getAccessRights() -> AccessRights? {
		guard let chat else { return nil }

		var auxiliaryData: AuxiliaryData?
		if let aux {
			auxiliaryData = AuxiliaryData(
				ageVerificationDate: aux.ageVerificationDate?.parseDate(format: aa2DateFormat),
				requiredAge: Int(aux.requiredAge ?? ""),
				validityDate: aux.validityDate?.parseDate(format: aa2DateFormat),
				communityId: aux.communityId
			)
		}

		let requiredRights = chat.required.compactMap { accessRight -> AccessRight? in
			AccessRight(rawValue: accessRight)
		}
		let optionalRights = chat.optional.compactMap { accessRight -> AccessRight? in
			AccessRight(rawValue: accessRight)
		}
		let effectiveRights = chat.effective.compactMap { accessRight -> AccessRight? in
			AccessRight(rawValue: accessRight)
		}

		return AccessRights(
			requiredRights: requiredRights,
			optionalRights: optionalRights,
			effectiveRights: effectiveRights,
			transactionInfo: transactionInfo,
			auxiliaryData: auxiliaryData
		)
	}

	func getAuthResult() -> AuthResult? {
		let result = getAuthResultData()

		var resultUrl: URL?
		if let url {
			resultUrl = URL(string: url)
		}

		if result != nil || resultUrl != nil {
			return AuthResult(
				url: resultUrl,
				result: result
			)
		}

		return nil
	}

	func getAuthResultData() -> AuthResultData? {
		guard let major = result?.major else { return nil }

		let minor = result?.minor
		let description = result?.description
		let message = result?.message
		let reason = result?.reason
		let language = result?.language

		return AuthResultData(
			major: major,
			minor: minor,
			language: language,
			description: description,
			message: message,
			reason: reason
		)
	}
}
