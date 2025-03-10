.. _workflow-controller-data-types:

WorkflowController data types
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. _wc-auth-result:

AuthResult
----------

Final result of an authentication.

- **url**:
  Refresh url or an optional communication error address.

- **result**:
  Contains information about the result of the authentication.
  See :ref:`wc-auth-result-data`.

.. tabs::

  .. code-tab:: kotlin

    data class AuthResult(
      val url: Uri?,
      val result: AuthResultData?
    ) : Parcelable

  .. code-tab:: swift

    public struct AuthResult {
      public let url: URL?
      public let result: AuthResultData?
    }

.. _wc-auth-result-data:

AuthResultData
--------------

Information about an authentication.


- **major**:
   Major error code. See BSI TR-03112 for error code definitions.

- **minor**:
   Minor error code. See BSI TR-03112 for error code definitions.

- **language**:
   Language of description and message. Language “en” is supported only at the moment.

- **description**:
   Description of the error message.

- **message**:
   The error message.

- **reason**:
   An unique reason why the workflow was aborted. See :external:doc:`failurecodes` of the AusweisApp SDK.


.. tabs::

  .. code-tab:: kotlin

    data class AuthResultData(
      val major: String,
      val minor: String?,
      val language: String?,
      val description: String?,
      val message: String?,
      val reason: String?
    ) : Parcelable

  .. code-tab:: swift

    public struct AuthResultData {
      public let major: String
      public let minor: String?
      public let language: String?
      public let description: String?
      public let message: String?
      public let reason: String?
    }

.. _wc-change-pin-result:

ChangePinResult
---------------

Final result of a PIN change.

- **success**:
  True if a the PIN has been successfully set, else false

- **Reason**:
  Unique error code if the PIN change failed.  See :external:doc:`failurecodes` of the AusweisApp SDK.

.. tabs::

  .. code-tab:: kotlin

    data class ChangePinResult(
      val success: Boolean,
      val reason: String?
    ) : Parcelable

  .. code-tab:: swift

    public struct ChangePinResult {
      public let success: Bool
      public let reason: String?
    }

.. _wc-certificate-description:

CertificateDescription
----------------------

Detailed description of the certificate.

- **issuerName**:
  Name of the certificate issuer.

- **issuerUrl**:
  URL of the certificate issuer.

- **subjectName**:
  Name of the certificate subject.

- **subjectUrl**:
  URL of the certificate subject.

- **termsOfUsage**:
  Raw certificate information about the terms of usage.

- **purpose**:
  Parsed purpose of the terms of usage.

- **validity**:
  Certificate validity, see :ref:`wc-certificate-validity`.

.. tabs::

  .. code-tab:: kotlin

    data class CertificateDescription(
        val issuerName: String,
        val issuerUrl: Uri?,
        val purpose: String,
        val subjectName: String,
        val subjectUrl: Uri?,
        val termsOfUsage: String,
        val validity: CertificateValidity
    ) : Parcelable

  .. code-tab:: swift

    public struct CertificateDescription {
        public let issuerName: String
        public let issuerUrl: URL?
        public let purpose: String
        public let subjectName: String
        public let subjectUrl: URL?
        public let termsOfUsage: String
        public let validity: CertificateValidity
    }

.. _wc-certificate-validity:

CertificateValidity
-------------------

Validity dates of the certificate in UTC.

- **effectiveDate**:
  Certificate is valid since this date.

- **expirationDate**:
  Certificate is invalid after this date.


.. tabs::

  .. code-tab:: kotlin

    data class CertificateValidity(
        val effectiveDate: Date,
        val expirationDate: Date
    ) : Parcelable

  .. code-tab:: swift

    public struct CertificateValidity {
        public let effectiveDate: Date
        public let expirationDate: Date
    }

.. _wc-access-rights:

AccessRights
------------

Access rights requested by the provider. See section :ref:`wc-access-right`.

- **requiredRights**:
  These rights are mandatory and cannot be disabled.

- **optionalRights**:
  These rights are optional and can be enabled or disabled

- **effectiveRights**:
  Indicates the enabled access rights of optional and required.

- **transactionInfo**:
  Optional transaction information.

- **auxiliaryData**:
  Optional auxiliary data of the provider, see :ref:`wc-auxiliary-data`.

.. tabs::

  .. code-tab:: kotlin

    data class AccessRights(
        val requiredRights: List<AccessRight>,
        val optionalRights: List<AccessRight>,
        val effectiveRights: List<AccessRight>,
        val transactionInfo: String?,
        val auxiliaryData: AuxiliaryData?
    ) : Parcelable

  .. code-tab:: swift

    public struct AccessRights {
        public let requiredRights: [AccessRight]
        public let optionalRights: [AccessRight]
        public let effectiveRights: [AccessRight]
        public let transactionInfo: String?
        public let auxiliaryData: AuxiliaryData?
    }

.. _wc-auxiliary-data:

AuxiliaryData
-------------

Auxiliary data of the provider.

- **ageVerificationDate**:
  Optional required date of birth for AgeVerification.

- **requiredAge**:
  Optional required age for AgeVerification.
  It is calculated by the SDK on the basis of ageVerificationDate and current date.

- **validityDate**:
  Optional validity date.

- **communityId**:
  Optional id of community.

.. tabs::

  .. code-tab:: kotlin

    data class OptionalProviderData(
        val ageVerificationDate: Date?,
        val requiredAge: Int?,
        val validityDate: Date?,
        val communityId: String?
    ) : Parcelable

  .. code-tab:: swift

    public struct OptionalProviderData {
        public let ageVerificationDate: Date?
        public let requiredAge: Int?
        public let validityDate: Date?
        public let communityId: String?
    }

.. _wc-reader:

Reader
------

Provides information about a reader.

- **name**:
  Identifier of card reader.

- **insertable**:
  Indicates whether a card can be inserted via :ref:`wc-set-card`.

- **attached**:
  Indicates whether a card reader is connected or disconnected.

- **keypad**:
  Indicates whether a card reader has a keypad. The parameter is only shown when a reader is attached.

- **card**:
  Provides information about inserted card, otherwise null.
  All properties of card set to null signal an unknown card.
  See :ref:`wc-card`.

.. tabs::

  .. code-tab:: kotlin

    data class Reader(
        val name: String,
        val insertable: Boolean,
        val attached: Boolean,
        val keypad: Boolean,
        val card: Card?
    ) : Parcelable

  .. code-tab:: swift

    public struct Reader {
      public let name: String
      public let insertable: Bool
      public let attached: Bool
      public let keypad: Bool
      public let card: Card?
    }

.. _wc-card:

Card
----

Provides information about inserted card.

An unknown card (without eID function) is represented by all properties set to null.
You can use the convinience function `Card.isUnknown()` to check for an unknown card.

- **inoperative**:
  True if PUK is inoperative and cannot unblock PIN, otherwise false.
  This can be recognized if user enters a correct PUK only.
  It is not possible to read this data before a user tries to unblock the PIN.

- **deactivated**:
  True if eID functionality is deactivated, otherwise false.
  The scan dialog on iOS won't be closed if this is True. You need to
  call :ref:`wc-interrupt` yourself to show an error message.

- **pinRetryCounter**:
  Count of possible retries for the PIN. If you enter a PIN it will be decreased if PIN was incorrect.

.. tabs::

  .. code-tab:: kotlin

    data class Card(
        val deactivated: Boolean?,
        val inoperative: Boolean?,
        val pinRetryCounter: Int?
    ) : Parcelable

    fun Card.isUnknown() : Boolean = inoperative == null && deactivated == null && pinRetryCounter == null

  .. code-tab:: swift

    public struct Card {
        public let deactivated: Bool?
        public let inoperative: Bool?
        public let pinRetryCounter: Int?

        public func isUnknown() -> Bool {
          return inoperative == nil && deactivated == nil && pinRetryCounter == nil
        }
    }

.. _wc-cause:

Cause
-----

Provides information about why the SDK is waiting.
See :ref:`wc-on-pause`.

List of all possible causes:

- BadCardPosition: Denotes an unstable or lost card connection.

.. tabs::

  .. code-tab:: kotlin

    enum class Cause(val rawName: String) {
      BadCardPosition("BadCardPosition"),
      ;
    }

  .. code-tab:: swift

    public enum Cause: String {
      case BadCardPosition
    }


.. _wc-wrapper-error:

WrapperError
------------

Provides information about an error.

- **msg**:
  Message type in which the error occurred.

- **error**:
  Error message.

.. tabs::

  .. code-tab:: kotlin

    data class WrapperError(
        val msg: String,
        val error: String
    ) : Parcelable

  .. code-tab:: swift

    public struct WrapperError {
      public let msg: String
      public let error: String
    }

.. _wc-workflowprogress:

WorkflowProgress
----------------

Provides information about the workflow status.

- **workflow**:
  Type of the current workflow.
  If there is no workflow in progress this will be null.

  See section :ref:`wc-workflowprogresstype`.

- **progress**:
  Percentage of workflow progress.
  If there is no workflow in progress this will be null.

- **state**:
  Name of the current state if paused.
  If there is no workflow in progress or the workflow is not paused this will be null.

.. tabs::

  .. code-tab:: kotlin

    data class WorkflowProgress(
      val workflow: WorkflowProgressType?,
      val progress: Int?,
      val state: String?
    ) : Parcelable

  .. code-tab:: swift

    public struct WorkflowProgress {
      public let workflow: WorkflowProgressType?
      public let progress: Int?
      public let state: String?
    }

.. _wc-workflowprogresstype:

WorkflowProgressType
--------------------

List of all types of WorkflowProgress

.. tabs::

  .. code-tab:: kotlin

    enum class WorkflowProgressType(val rawName: String) {
      AUTHENTICATION("AUTH"),
      CHANGE_PIN("CHANGE_PIN");

      [...]
    }

  .. code-tab:: swift

    public enum WorkflowProgressType: String {
      case AUTHENTICATION = "AUTH"
      case CHANGE_PIN = "CHANGE_PIN"
    }

.. _wc-userinfomessages:

AA2UserInfoMessages
-------------------

| *iOS only*
| Messages for the NFC system dialog on iOS

- **sessionStarted**:
  Shown if scanning is started
- **sessionFailed**:
  Shown if communication was stopped with an error.
- **sessionSucceeded**:
  Shown if communication was stopped successfully.
- **sessionInProgress**:
  Shown if communication is in progress. This message will be appended with current percentage level.

.. tabs::

  .. code-tab:: swift

    public struct AA2UserInfoMessages: Encodable {
      let sessionStarted: String?
      let sessionFailed: String?
      let sessionSucceeded: String?
      let sessionInProgress: String?

      [...]
    }

.. _wc-simulator:

Simulator
---------

Optional definition of files for the Simulator reader

Also see SDK documentation of Simulator :external:ref:`filesystem`.

- **files**:
  List of Filesystem definitions.

  The files from :external:ref:`advanced` will always be present
  and can be overwritten as they are required for successful authentication.
  All other files will not exist unless they are specified so it is possible to
  simulate a missing piece of personal data.

  See :ref:`wc-simulator-file`.

- **keys**:
  Optional list of SimulatorKey definitions.

  All keys will always be present and can be overwritten as they are required for
  successful authentication.

  See :ref:`wc-simulator-key`.

.. tabs::

  .. code-tab:: kotlin

    data class Simulator (
      val files: List<SimulatorFile>,
      val keys: List<SimulatorKey>?,
    ) : Parcelable

  .. code-tab:: swift

    public struct Simulator: Encodable {
      let files: [SimulatorFile]
      let keys: [SimulatorKey]?
    }

.. _wc-simulator-file:

SimulatorFile
-------------

Filesystem for Simulator reader

The content of the filesystem can be provided as a JSON array of objects.
The ``fileId`` and ``shortFileId`` are specified on the corresponding technical guideline
of the BSI and ISO. The ``content`` is an ASN.1 structure in DER encoding.

All fields are hex encoded. Also see SDK documentation of Simulator :external:ref:`filesystem`.

.. tabs::

  .. code-tab:: kotlin

    data class SimulatorFile (
      val fileId: String,
      val shortFileId: String,
      val content: String
    ) : Parcelable

  .. code-tab:: swift

    public struct SimulatorFile: Encodable {
      let fileId: String
      let shortFileId: String
      let content: String
    }

.. seealso::
  ISO 7816-4:2005 8.2.1.1

  `TR-03110_part3`_, part 3: Section A.1.2. Storage on the Chip

  `TR-03110_part4`_, part 4: Applications and Document Profiles

  .. _TR-03110_part3: https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03110/BSI_TR-03110_Part-3-V2_2.pdf
  .. _TR-03110_part4: https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03110/BSI_TR-03110_Part-4_V2-2.pdf

.. _wc-simulator-key:

SimulatorKey
------------

Keys for Simulator reader

- **id**:
  id of the key.

  The Key ``1`` is used to check the blacklist while key ``2`` is used
  to calculate the pseudonym for the service provider.

- **content**:
  The hex encoded key.

  A new key can be generated with OpenSSL (convert base64 to hex after generation).

  .. code-block:: console

    openssl genpkey -algorithm ec -pkeyopt ec_paramgen_curve:brainpoolP256r1

.. tabs::

  .. code-tab:: kotlin

    data class SimulatorKey(
        val id: Int,
        val content: String,
    ) : Parcelable

  .. code-tab:: swift

    public struct SimulatorKey: Encodable {
      let id: Int
      let content: String
    }

.. _wc-versioninfo:

VersionInfo
-----------

Provides information about the underlying AusweisApp

- **name**:
  Application name.

- **implementationTitle**:
  Title of implementation.

- **implementationVendor**:
  Vendor of implementation.

- **implementationVersion**:
  Version of implementation.

- **specificationTitle**:
  Title of specification.

- **specificationVendor**:
  Vendor of specification.

- **specificationVersion**:
  Version of specification.

.. tabs::

  .. code-tab:: kotlin

    data class VersionInfo(
        val name: String,
        val implementationTitle: String,
        val implementationVendor: String,
        val implementationVersion: String,
        val specificationVendor: String,
        val specificationVersion: String
    ) : Parcelable

  .. code-tab:: swift

    public struct VersionInfo {
      public let name: String
      public let implementationTitle: String
      public let implementationVendor: String
      public let implementationVersion: String
      public let specificationTitle: String
      public let specificationVendor: String
      public let specificationVersion: String
      [...]
    }

.. _wc-access-right:

AccessRight
-----------

List of all available access rights a provider might request.

.. tabs::

  .. code-tab:: kotlin

    enum class AccessRight(val rawName: String) {
      ADDRESS("Address"),
      BIRTH_NAME("BirthName"),
      FAMILY_NAME("FamilyName"),
      GIVEN_NAMES("GivenNames"),
      PLACE_OF_BIRTH("PlaceOfBirth"),
      DATE_OF_BIRTH("DateOfBirth"),
      DOCTORAL_DEGREE("DoctoralDegree"),
      ARTISTIC_NAME("ArtisticName"),
      PSEUDONYM("Pseudonym"),
      VALID_UNTIL("ValidUntil"),
      NATIONALITY("Nationality"),
      ISSUING_COUNTRY("IssuingCountry"),
      DOCUMENT_TYPE("DocumentType"),
      RESIDENCE_PERMIT_I("ResidencePermitI"),
      RESIDENCE_PERMIT_II("ResidencePermitII"),
      COMMUNITY_ID("CommunityID"),
      ADDRESS_VERIFICATION("AddressVerification"),
      AGE_VERIFICATION("AgeVerification"),
      WRITE_ADDRESS("WriteAddress"),
      WRITE_COMMUNITY_ID("WriteCommunityID"),
      WRITE_RESIDENCE_PERMIT_I("WriteResidencePermitI"),
      WRITE_RESIDENCE_PERMIT_II("WriteResidencePermitII"),
      CAN_ALLOWED("CanAllowed"),
      PIN_MANAGEMENT("PinManagement");

      [...]
    }

  .. code-tab:: swift

    public enum AccessRight: String {
        case Address,
        BirthName,
        FamilyName,
        GivenNames,
        PlaceOfBirth,
        DateOfBirth,
        DoctoralDegree,
        ArtisticName,
        Pseudonym,
        ValidUntil,
        Nationality,
        IssuingCountry,
        DocumentType,
        ResidencePermitI,
        ResidencePermitII,
        CommunityID,
        AddressVerification,
        AgeVerification,
        WriteAddress,
        WriteCommunityID,
        WriteResidencePermitI,
        WriteResidencePermitII,
        CanAllowed,
        PinManagement
    }
