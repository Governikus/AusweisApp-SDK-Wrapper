/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core

internal open class TestWorkflowCallbacks : WorkflowCallbacks {
    override fun onStarted() {}

    override fun onAuthenticationStarted() {}

    override fun onAuthenticationStartFailed(error: String) {}

    override fun onChangePinStarted() {}

    override fun onAccessRights(
        error: String?,
        accessRights: AccessRights?,
    ) {}

    override fun onCertificate(certificateDescription: CertificateDescription) {}

    override fun onInsertCard(error: String?) {}

    override fun onReader(reader: Reader?) {}

    override fun onReaderList(readers: List<Reader>?) {}

    override fun onEnterPin(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterNewPin(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterPuk(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterCan(
        error: String?,
        reader: Reader,
    ) {}

    override fun onAuthenticationCompleted(authResult: AuthResult) {}

    override fun onChangePinCompleted(changePinResult: ChangePinResult) {}

    override fun onWrapperError(error: WrapperError) {}

    override fun onStatus(workflowProgress: WorkflowProgress) {}

    override fun onInfo(
        versionInfo: VersionInfo,
        connectionInfo: ConnectionInfo,
    ) {}

    override fun onBadState(error: String) {}

    override fun onInternalError(error: String) {}

    override fun onPause(cause: Cause) {}
}
