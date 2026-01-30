/**
 * Copyright (c) 2024-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.sdk.jsonobjects

interface Command {
    val cmd: String
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
    val value: String,
    override val cmd: String = "SET_CAN",
) : Command

class SetPin(
    val value: String,
    override val cmd: String = "SET_PIN",
) : Command

class SetPuk(
    val value: String,
    override val cmd: String = "SET_PUK",
) : Command

class SetCard(
    val name: String,
    override val cmd: String = "SET_CARD",
) : Command
