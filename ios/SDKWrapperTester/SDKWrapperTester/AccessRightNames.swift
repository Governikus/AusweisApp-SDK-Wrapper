/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import AusweisApp2SDKWrapper
import Foundation
import SwiftUI

// swiftlint:disable line_length

public enum AccessRightNames {
	static let pretty: [AccessRight: String] = [
		AccessRight.Address: NSLocalizedString("Address", comment: "Pretty name of Address"),
		AccessRight.BirthName: NSLocalizedString("Birth name", comment: "Pretty name of BirthName"),
		AccessRight.FamilyName: NSLocalizedString("Family name", comment: "Pretty name of FamilyName"),
		AccessRight.GivenNames: NSLocalizedString("Given name(s)", comment: "Pretty name of GivenNames"),
		AccessRight.PlaceOfBirth: NSLocalizedString("Place of birth", comment: "Pretty name of PlaceOfBirth"),
		AccessRight.DateOfBirth: NSLocalizedString("Date of birth", comment: "Pretty name of DateOfBirth"),
		AccessRight.DoctoralDegree: NSLocalizedString("Doctoral degree", comment: "Pretty name of DoctoralDegree"),
		AccessRight.ArtisticName: NSLocalizedString("Religious / artistic name", comment: "Pretty name of ArtisticName"),
		AccessRight.Pseudonym: NSLocalizedString("Pseudonym", comment: "Pretty name of Pseudonym"),
		AccessRight.ValidUntil: NSLocalizedString("Valid until", comment: "Pretty name of ValidUntil"),
		AccessRight.Nationality: NSLocalizedString("Nationality", comment: "Pretty name of Nationality"),
		AccessRight.IssuingCountry: NSLocalizedString("Issuing country", comment: "Pretty name of IssuingCountry"),
		AccessRight.DocumentType: NSLocalizedString("Document type", comment: "Pretty name of DocumentType"),
		AccessRight.ResidencePermitI: NSLocalizedString("Residence permit I", comment: "Pretty name of ResidencePermitI"),
		AccessRight.ResidencePermitII: NSLocalizedString("Residence permit II", comment: "Pretty name of ResidencePermitII"),
		AccessRight.CommunityID: NSLocalizedString("Community-ID", comment: "Pretty name of CommunityID"),
		AccessRight.AddressVerification: NSLocalizedString("Address verification", comment: "Pretty name of AddressVerification"),
		AccessRight.AgeVerification: NSLocalizedString("Age verification", comment: "Pretty name of AgeVerification"),
		AccessRight.WriteAddress: NSLocalizedString("Write address", comment: "Pretty name of WriteAddress"),
		AccessRight.WriteCommunityID: NSLocalizedString("Write community-ID", comment: "Pretty name of WriteCommunityID"),
		AccessRight.WriteResidencePermitI: NSLocalizedString("Write residence permit I", comment: "Pretty name of WriteResidencePermitI"),
		AccessRight.WriteResidencePermitII: NSLocalizedString("Write residence permit II", comment: "Pretty name of WriteResidencePermitII"),
		AccessRight.CanAllowed: NSLocalizedString("CAN-allowed", comment: "Pretty name of CanAllowed"),
		AccessRight.PinManagement: NSLocalizedString("PIN management", comment: "Pretty name of PinManagement")
	]
}

// swiftlint:enable line_length
