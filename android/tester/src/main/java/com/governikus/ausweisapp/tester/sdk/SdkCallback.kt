/*
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.sdk

import com.governikus.ausweisapp2.IAusweisApp2SdkCallback

abstract class SdkCallback : IAusweisApp2SdkCallback.Stub() {
    var sessionID: String? = null
        private set

    override fun sessionIdGenerated(
        sessionId: String,
    ) {
        this.sessionID = sessionId
    }
}
