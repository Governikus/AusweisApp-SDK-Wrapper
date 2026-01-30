/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.common

open class LiveDataEvent<out T>(
    private val content: T,
) {
    var handled = false
        private set

    fun getContentIfNotHandled(): T? =
        if (handled) {
            null
        } else {
            handled = true
            content
        }

    fun peekContent(): T = content
}
