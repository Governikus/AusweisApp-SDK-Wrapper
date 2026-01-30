/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol

import com.squareup.moshi.Json

internal object Messages {
    const val MSG_ACCESS_RIGHTS = "ACCESS_RIGHTS"
    const val MSG_AUTH = "AUTH"
    const val MSG_BAD_STATE = "BAD_STATE"
    const val MSG_CERTIFICATE = "CERTIFICATE"
    const val MSG_CHANGE_PIN = "CHANGE_PIN"
    const val MSG_ENTER_CAN = "ENTER_CAN"
    const val MSG_ENTER_NEW_PIN = "ENTER_NEW_PIN"
    const val MSG_INFO = "INFO"
    const val MSG_ENTER_PIN = "ENTER_PIN"
    const val MSG_ENTER_PUK = "ENTER_PUK"
    const val MSG_INSERT_CARD = "INSERT_CARD"
    const val MSG_INTERNAL_ERROR = "INTERNAL_ERROR"
    const val MSG_INVALID = "INVALID"
    const val MSG_PAUSE = "PAUSE"
    const val MSG_READER = "READER"
    const val MSG_READER_LIST = "READER_LIST"
    const val MSG_STATUS = "STATUS"
    const val MSG_UNKNOWN_COMMAND = "UNKNOWN_COMMAND"
}

internal data class Message(
    val attached: Boolean?,
    val aux: Aux?,
    val card: Card?,
    val cause: String?,
    val chat: Chat?,
    val description: Description?,
    val error: String?,
    val insertable: Boolean?,
    val keypad: Boolean?,
    val msg: String?,
    val name: String?,
    val progress: Int?,
    val reader: Reader?,
    val readers: List<Reader>?,
    val reason: String?,
    val result: Result?,
    val state: String?,
    val success: Boolean?,
    val transactionInfo: String?,
    val url: String?,
    val validity: Validity?,
    @Json(name = "VersionInfo")
    val versionInfo: VersionInfo?,
    val workflow: String?,
)

internal data class Description(
    val issuerName: String,
    val issuerUrl: String,
    val purpose: String,
    val subjectName: String,
    val subjectUrl: String,
    val termsOfUsage: String,
)

internal data class Validity(
    val effectiveDate: String,
    val expirationDate: String,
)

internal data class Chat(
    val effective: List<String>,
    val optional: List<String>,
    val required: List<String>,
)

internal data class Aux(
    val ageVerificationDate: String?,
    val requiredAge: String?,
    val validityDate: String?,
    val communityId: String?,
)

internal data class Card(
    val inoperative: Boolean?,
    val deactivated: Boolean?,
    val retryCounter: Int?,
)

internal data class Reader(
    val name: String,
    val insertable: Boolean,
    val attached: Boolean,
    val keypad: Boolean,
    val card: Card?,
)

internal data class Result(
    val major: String?,
    val minor: String?,
    val language: String?,
    val description: String?,
    val message: String?,
    val reason: String?,
)

internal data class VersionInfo(
    @Json(name = "Name")
    val name: String,
    @Json(name = "Implementation-Title")
    val implementationTitle: String,
    @Json(name = "Implementation-Vendor")
    val implementationVendor: String,
    @Json(name = "Implementation-Version")
    val implementationVersion: String,
    @Json(name = "Specification-Title")
    val specificationTitle: String,
    @Json(name = "Specification-Vendor")
    val specificationVendor: String,
    @Json(name = "Specification-Version")
    val specificationVersion: String,
)
