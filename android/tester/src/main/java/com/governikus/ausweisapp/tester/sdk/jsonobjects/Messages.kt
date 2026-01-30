/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.sdk.jsonobjects

data class Message(
    val msg: String,
    val result: Result?,
    val cause: String?,
    val url: String?,
)

data class Result(
    val minor: String?,
)

data class Reader(
    val msg: String,
    val attached: Boolean,
    val insertable: Boolean,
    val name: String,
    val card: Card?,
)

data class Card(
    val inoperative: Boolean?,
    val deactivated: Boolean?,
    val retryCounter: Int?,
)
