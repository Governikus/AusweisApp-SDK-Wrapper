.. _setup-ios:

Setup iOS
=========

1. Add the "AusweisApp SDK Wrapper" swift-package as dependency to your XCode project.

   .. code-block:: text

      https://github.com/Governikus/AusweisApp2Wrapper-iOS-SPM

2. Make sure that the app entitlements include the `NFC Tag Reader Session <https://developer.apple.com/documentation/bundleresources/entitlements/com_apple_developer_nfc_readersession_formats>`_.


3. Define in your app's Info.plist, which NFC tag(s) the app wants to read (`see TR-03110-4 chapter 2.2.1 <https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03110/BSI_TR-03110_Part-4_V2-2.pdf?__blob=publicationFile&v=2>`_).

   .. code-block:: xml

      <key>com.apple.developer.nfc.readersession.iso7816.select-identifiers</key>
      <array>
         <string>E80704007F00070302</string>
      </array>

4. Add NFC description to your app's Info.plist

   .. code-block:: xml

      <key>NFCReaderUsageDescription</key>
      <string>Scan ID card</string>
