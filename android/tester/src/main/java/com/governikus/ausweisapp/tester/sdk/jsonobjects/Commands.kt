/*
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.sdk.jsonobjects

interface Command {
    val cmd: String
}

interface SensitiveCommand : Command {
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

class Accept(
    override val cmd: String = "ACCEPT",
) : Command

class Cancel(
    override val cmd: String = "CANCEL",
) : Command

class Continue(
    override val cmd: String = "CONTINUE",
) : Command

class GetAccessRights(
    override val cmd: String = "GET_ACCESS_RIGHTS",
) : Command

class GetApiLevel(
    override val cmd: String = "GET_API_LEVEL",
) : Command

class GetCertificate(
    override val cmd: String = "GET_CERTIFICATE",
) : Command

class GetInfo(
    override val cmd: String = "GET_INFO",
) : Command

class GetReader(
    override val cmd: String = "GET_READER",
) : Command

class GetReaderList(
    override val cmd: String = "GET_READER_LIST",
) : Command

class RunAuth(
    val tcTokenURL: String = "https://test.governikus-eid.de/AusweisAuskunft/WebServiceRequesterServlet",
    override val cmd: String = "RUN_AUTH",
) : Command

class SetAccessRights(
    val chat: List<String>,
    override val cmd: String = "SET_ACCESS_RIGHTS",
) : Command

class SetApiLevel(
    val level: Int,
    override val cmd: String = "SET_API_LEVEL",
) : Command

class SetCan(
    override val value: CharArray,
    override val cmd: String = "SET_CAN",
) : SensitiveCommand

class SetPin(
    override val value: CharArray?,
    override val cmd: String = "SET_PIN",
) : SensitiveCommand

class SetPuk(
    override val value: CharArray,
    override val cmd: String = "SET_PUK",
) : SensitiveCommand

class SetCard(
    val name: String,
    override val cmd: String = "SET_CARD",
) : Command
