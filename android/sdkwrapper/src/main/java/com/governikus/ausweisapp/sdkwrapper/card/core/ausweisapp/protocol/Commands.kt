/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol

internal interface Command {
    val cmd: String
}

internal interface SensitiveCommand : Command {
    val value: CharArray?

    fun toJsonCharArray(): CharArray {
        val prefixStr = "{\"cmd\":\"$cmd\",\"value\":"
        val currentValue = value

        if (currentValue == null) {
            return "$prefixStr null}".toCharArray()
        }

        val prefix = "$prefixStr\"".toCharArray()
        val suffix = "\"}".toCharArray()

        val result = CharArray(prefix.size + currentValue.size + suffix.size)
        prefix.copyInto(result, destinationOffset = 0)
        currentValue.copyInto(result, destinationOffset = prefix.size)
        suffix.copyInto(result, destinationOffset = prefix.size + currentValue.size)

        return result
    }

    fun clear() {
        value?.fill('\u0000')
    }
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
    override val value: CharArray?,
    override val cmd: String = "SET_CAN",
) : SensitiveCommand

internal class SetCard(
    val name: String,
    val simulator: Simulator?,
    override val cmd: String = "SET_CARD",
) : Command

internal class SetPin(
    override val value: CharArray?,
    override val cmd: String = "SET_PIN",
) : SensitiveCommand

internal class SetNewPin(
    override val value: CharArray?,
    override val cmd: String = "SET_NEW_PIN",
) : SensitiveCommand

internal class SetPuk(
    override val value: CharArray?,
    override val cmd: String = "SET_PUK",
) : SensitiveCommand

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
