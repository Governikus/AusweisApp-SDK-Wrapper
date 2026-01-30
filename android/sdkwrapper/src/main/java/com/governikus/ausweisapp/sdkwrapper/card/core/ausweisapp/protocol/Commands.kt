/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol

internal interface Command {
    val cmd: String
}

internal class Accept(
    override val cmd: String = "ACCEPT",
) : Command

internal class Cancel(
    override val cmd: String = "CANCEL",
) : Command

internal class ContinueWorkflow(
    override val cmd: String = "CONTINUE",
) : Command

internal class GetCertificate(
    override val cmd: String = "GET_CERTIFICATE",
) : Command

internal class RunAuth(
    val tcTokenURL: String,
    val developerMode: Boolean,
    val status: Boolean,
    override val cmd: String = "RUN_AUTH",
) : Command

internal class RunChangePin(
    val status: Boolean,
    override val cmd: String = "RUN_CHANGE_PIN",
) : Command

internal class GetAccessRights(
    override val cmd: String = "GET_ACCESS_RIGHTS",
) : Command

internal class SetAccessRights(
    val chat: List<String>,
    override val cmd: String = "SET_ACCESS_RIGHTS",
) : Command

internal class SetCan(
    val value: String?,
    override val cmd: String = "SET_CAN",
) : Command

internal class SetCard(
    val name: String,
    val simulator: Simulator?,
    override val cmd: String = "SET_CARD",
) : Command

internal class SetPin(
    val value: String?,
    override val cmd: String = "SET_PIN",
) : Command

internal class SetNewPin(
    val value: String?,
    override val cmd: String = "SET_NEW_PIN",
) : Command

internal class SetPuk(
    val value: String?,
    override val cmd: String = "SET_PUK",
) : Command

internal class GetStatus(
    override val cmd: String = "GET_STATUS",
) : Command

internal class GetInfo(
    override val cmd: String = "GET_INFO",
) : Command

internal class GetReader(
    val name: String,
    override val cmd: String = "GET_READER",
) : Command

internal class GetReaderList(
    override val cmd: String = "GET_READER_LIST",
) : Command
