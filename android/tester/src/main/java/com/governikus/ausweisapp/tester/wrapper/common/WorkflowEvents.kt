/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.common

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData

internal data class NavEvent(
    val action: Int,
    val data: Bundle? = null,
)

internal class NavLiveEvent : MutableLiveData<LiveDataEvent<NavEvent>>()

internal class WorkflowLiveEvent : MutableLiveData<LiveDataEvent<NavEvent>>()

internal class ToastLiveEvent : MutableLiveData<LiveDataEvent<String>>()

internal fun NavLiveEvent.navigate(
    @IdRes action: Int,
    data: Bundle? = null,
) {
    value = LiveDataEvent(NavEvent(action, data))
}

internal fun WorkflowLiveEvent.finished(
    resultCode: Int,
    data: Bundle? = null,
) {
    value = LiveDataEvent(NavEvent(resultCode, data))
}

internal fun ToastLiveEvent.show(text: String) {
    value = LiveDataEvent(text)
}
