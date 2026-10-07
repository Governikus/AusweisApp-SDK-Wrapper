/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp

import android.net.Uri
import com.governikus.ausweisapp.sdkwrapper.card.core.AccessRight
import com.governikus.ausweisapp.sdkwrapper.card.core.AccessRights
import com.governikus.ausweisapp.sdkwrapper.card.core.AuthResult
import com.governikus.ausweisapp.sdkwrapper.card.core.AuthResultData
import com.governikus.ausweisapp.sdkwrapper.card.core.AuxiliaryData
import com.governikus.ausweisapp.sdkwrapper.card.core.Card
import com.governikus.ausweisapp.sdkwrapper.card.core.CertificateDescription
import com.governikus.ausweisapp.sdkwrapper.card.core.CertificateValidity
import com.governikus.ausweisapp.sdkwrapper.card.core.Reader
import com.governikus.ausweisapp.sdkwrapper.card.core.VersionInfo
import com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowProgress
import com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowProgressType
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Message
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)

internal fun Message.getCertificateDescription(): CertificateDescription? {
    val description = description ?: return null
    val validity = validity ?: return null
    val issueDate = dateFormat.parse(validity.effectiveDate) ?: return null
    val expirationDate = dateFormat.parse(validity.expirationDate) ?: return null

    val issuerUrl = if (description.issuerUrl.isNotBlank()) Uri.parse(description.issuerUrl) else null
    val subjectUrl = if (description.subjectUrl.isNotBlank()) Uri.parse(description.subjectUrl) else null

    return CertificateDescription(
        issuerName = description.issuerName,
        issuerUrl = issuerUrl,
        purpose = description.purpose,
        subjectName = description.subjectName,
        subjectUrl = subjectUrl,
        termsOfUsage = description.termsOfUsage,
        validity =
            CertificateValidity(
                effectiveDate = issueDate,
                expirationDate = expirationDate,
            ),
    )
}

internal fun Message.getCard(): Card? {
    val card = card ?: reader?.card ?: return null

    return Card(
        deactivated = card.deactivated,
        inoperative = card.inoperative,
        pinRetryCounter = card.retryCounter,
    )
}

internal fun Message.getReaderFromRoot(): Reader? {
    val name = name ?: return null
    val insertable = insertable ?: false
    val attached = attached ?: false
    val keypad = keypad ?: false

    return Reader(
        name = name,
        insertable = insertable,
        attached = attached,
        keypad = keypad,
        card = getCard(),
    )
}

internal fun Message.getReaderFromReaderMember(): Reader? {
    val reader = reader ?: return null
    val name = reader.name
    val insertable = reader.insertable
    val keypad = reader.keypad
    val attached = reader.attached

    return Reader(
        name = name,
        insertable = insertable,
        attached = attached,
        keypad = keypad,
        card = getCard(),
    )
}

internal fun Message.getReaderList(): List<Reader>? {
    val readers = readers ?: return null

    return readers.map {
        val card =
            if (it.card == null) {
                null
            } else {
                Card(
                    deactivated = it.card.deactivated,
                    inoperative = it.card.inoperative,
                    pinRetryCounter = it.card.retryCounter,
                )
            }
        Reader(
            name = it.name,
            insertable = it.insertable,
            attached = it.attached,
            keypad = it.keypad,
            card = card,
        )
    }
}

internal fun Message.getVersionInfo(): VersionInfo? {
    val info = versionInfo ?: return null

    return VersionInfo(
        name = info.name,
        implementationTitle = info.implementationTitle,
        implementationVendor = info.implementationVendor,
        implementationVersion = info.implementationVersion,
        specificationTitle = info.specificationTitle,
        specificationVendor = info.specificationVendor,
        specificationVersion = info.specificationVersion,
    )
}

internal fun Message.getAccessRights(): AccessRights? {
    val chat = chat ?: return null

    val auxiliaryData =
        aux?.run {
            AuxiliaryData(
                ageVerificationDate = if (ageVerificationDate != null) dateFormat.parse(ageVerificationDate) else null,
                requiredAge = requiredAge?.toInt(),
                validityDate = if (validityDate != null) dateFormat.parse(validityDate) else null,
                communityId = communityId,
            )
        }

    val requiredRights = chat.required.mapNotNull { AccessRight.fromRawName(rawName = it) }
    val optionalRights = chat.optional.mapNotNull { AccessRight.fromRawName(rawName = it) }
    val effectiveRights = chat.effective.mapNotNull { AccessRight.fromRawName(rawName = it) }

    return AccessRights(
        requiredRights = requiredRights,
        optionalRights = optionalRights,
        effectiveRights = effectiveRights,
        transactionInfo = transactionInfo,
        auxiliaryData = auxiliaryData,
    )
}

internal fun Message.getAuthResult(): AuthResult? {
    val uri = if (url != null) Uri.parse(url) else null
    val resultData = getAuthResultData()

    if (resultData != null || uri != null) {
        return AuthResult(url = uri, result = resultData)
    }

    return null
}

internal fun Message.getAuthResultData(): AuthResultData? {
    val result = result ?: return null
    if (result.major == null) return null

    return AuthResultData(
        major = result.major,
        minor = result.minor,
        language = result.language,
        description = result.description,
        message = result.message,
        reason = result.reason,
    )
}

internal fun Message.getWorkflowProgress(): WorkflowProgress {
    val workflowType = WorkflowProgressType.fromRawName(rawName = workflow)
    return WorkflowProgress(workflow = workflowType, progress = progress, state = state)
}
