/*
 * Copyright (c) 2026 Governikus Service GmbH, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp

import android.nfc.Tag
import android.os.RemoteException
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Accept
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Cancel
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Command
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.ContinueWorkflow
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetAccessRights
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetCertificate
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetInfo
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetReader
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetReaderList
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.GetStatus
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.RunAuth
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.RunChangePin
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetAccessRights
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetCan
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetCard
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetNewPin
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetPin
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetPuk
import com.governikus.ausweisapp2.IAusweisApp2Sdk
import com.governikus.ausweisapp2.IAusweisApp2SdkCallback
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeSdk : IAusweisApp2Sdk.Stub() {
    var capturedSessionId: String? = null
    var capturedMessageFromClient: CharArray? = null
    var capturedMessageFromClientAsString: String? = null
    var shouldThrowException = false

    @Deprecated(
        message = "Use transmit instead.",
        replaceWith = ReplaceWith(expression = "transmit(pMessageFromClient?.toCharArray())"),
    )
    override fun send(
        pSessionId: String,
        pMessageFromClient: String,
    ): Boolean = false

    override fun transmit(
        pSessionId: String,
        pMessageFromClient: CharArray,
    ): Boolean {
        if (shouldThrowException) throw RemoteException("Simulated IPC failure")

        capturedSessionId = pSessionId
        capturedMessageFromClient = pMessageFromClient
        capturedMessageFromClientAsString = String(chars = pMessageFromClient)
        return true
    }

    override fun connectSdk(callback: IAusweisApp2SdkCallback?): Boolean = false

    override fun updateNfcTag(
        pSessionId: String,
        pTag: Tag,
    ): Boolean = false
}

@RunWith(value = RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class AA2SdkConnectionBasicTest {
    private lateinit var connection: AA2SdkConnection
    private lateinit var fakeSdk: FakeSdk

    @Before
    fun setup() {
        connection = AA2SdkConnection()
        fakeSdk = FakeSdk()

        connection.sdk = fakeSdk
        connection.sdkSessionId = "test-session-123"
    }

    @Test
    fun `send fails if sdk is null`() {
        connection.sdk = null
        val result =
            connection.send(
                command =
                    RunAuth(
                        tcTokenURL = "https://example.org",
                        developerMode = false,
                        status = true,
                        header = null,
                    ),
                clazz = RunAuth::class.java,
            )

        assertFalse("Expected send to fail when SDK is null", result)
        assertNull(fakeSdk.capturedMessageFromClientAsString)
    }

    @Test
    fun `send fails if sessionId is null`() {
        connection.sdkSessionId = null
        val result =
            connection.send(
                command =
                    RunAuth(
                        tcTokenURL = "https://example.org",
                        developerMode = false,
                        status = true,
                        header = null,
                    ),
                clazz = RunAuth::class.java,
            )

        assertFalse("Expected send to fail when sessionId is null", result)
        assertNull(fakeSdk.capturedMessageFromClientAsString)
    }

    @Test
    fun `send handles exceptions gracefully and returns false`() {
        fakeSdk.shouldThrowException = true
        val command = Accept()

        val result = connection.send(command = command, clazz = Accept::class.java)

        assertFalse("Expected send to return false on exception", result)
    }
}

@RunWith(value = ParameterizedRobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class AA2SdkConnectionNonSensitiveSerializationTest(
    private val commandAsAny: Any,
    private val clazzAsAny: Class<*>,
    private val expectedJson: String,
) {
    private lateinit var connection: AA2SdkConnection
    private lateinit var fakeSdk: FakeSdk

    @Before
    fun setup() {
        connection = AA2SdkConnection()
        fakeSdk = FakeSdk()

        connection.sdk = fakeSdk
        connection.sdkSessionId = "test-session-123"
    }

    @Test
    fun `commands are serialized into the correct JSON payload`() {
        val command = commandAsAny as Command

        @Suppress("UNCHECKED_CAST")
        val clazz = clazzAsAny as Class<Command>

        val result = connection.send(command = command, clazz = clazz)

        assertTrue("Expected send() to succeed", result)
        assertEquals(expectedJson, fakeSdk.capturedMessageFromClientAsString)
    }

    companion object {
        @JvmStatic
        @Parameters(name = "Testing {1} serialization")
        fun data(): Collection<Array<Any>> =
            listOf(
                arrayOf(
                    Accept(),
                    Accept::class.java,
                    """{"cmd":"ACCEPT"}""",
                ),
                arrayOf(
                    Cancel(),
                    Cancel::class.java,
                    """{"cmd":"CANCEL"}""",
                ),
                arrayOf(
                    ContinueWorkflow(),
                    ContinueWorkflow::class.java,
                    """{"cmd":"CONTINUE"}""",
                ),
                arrayOf(
                    GetAccessRights(),
                    GetAccessRights::class.java,
                    """{"cmd":"GET_ACCESS_RIGHTS"}""",
                ),
                arrayOf(
                    GetCertificate(),
                    GetCertificate::class.java,
                    """{"cmd":"GET_CERTIFICATE"}""",
                ),
                arrayOf(
                    GetInfo(),
                    GetInfo::class.java,
                    """{"cmd":"GET_INFO"}""",
                ),
                arrayOf(
                    GetReader(name = "reader123"),
                    GetReader::class.java,
                    """{"name":"reader123","cmd":"GET_READER"}""",
                ),
                arrayOf(
                    GetReaderList(),
                    GetReaderList::class.java,
                    """{"cmd":"GET_READER_LIST"}""",
                ),
                arrayOf(
                    GetStatus(),
                    GetStatus::class.java,
                    """{"cmd":"GET_STATUS"}""",
                ),
                arrayOf(
                    RunChangePin(status = true),
                    RunChangePin::class.java,
                    """{"status":true,"cmd":"RUN_CHANGE_PIN"}""",
                ),
                arrayOf(
                    SetAccessRights(chat = listOf()),
                    SetAccessRights::class.java,
                    """{"chat":[],"cmd":"SET_ACCESS_RIGHTS"}""",
                ),
                arrayOf(
                    SetCard(name = "card123", simulator = null),
                    SetCard::class.java,
                    """{"name":"card123","cmd":"SET_CARD"}""",
                ),
                arrayOf(
                    RunAuth(
                        tcTokenURL = "https://example.org",
                        developerMode = false,
                        status = true,
                        header = hashMapOf("Bearer" to "0123456789abcdef"),
                    ),
                    RunAuth::class.java,
                    """{"tcTokenURL":"https://example.org","developerMode":false,"status":true,"header":{"Bearer":"0123456789abcdef"},"cmd":"RUN_AUTH"}""",
                ),
            )
    }
}

@RunWith(value = ParameterizedRobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class AA2SdkConnectionSensitiveSerializationTest(
    private val commandAsAny: Any,
    private val clazzAsAny: Class<*>,
    private val expectedJson: String,
) {
    private lateinit var connection: AA2SdkConnection
    private lateinit var fakeSdk: FakeSdk

    @Before
    fun setup() {
        connection = AA2SdkConnection()
        fakeSdk = FakeSdk()

        connection.sdk = fakeSdk
        connection.sdkSessionId = "test-session-123"
    }

    @Test
    fun `sensitive commands are serialized into the correct JSON payload and data is cleared`() {
        val command = commandAsAny as Command

        @Suppress("UNCHECKED_CAST")
        val clazz = clazzAsAny as Class<Command>

        val result = connection.send(command = command, clazz = clazz)

        assertTrue("Expected send() to succeed", result)
        assertEquals(expectedJson, fakeSdk.capturedMessageFromClientAsString)
        assertArrayEquals(CharArray(size = fakeSdk.capturedMessageFromClient!!.size), fakeSdk.capturedMessageFromClient)
    }

    companion object {
        @JvmStatic
        @Parameters(name = "Testing {1} serialization")
        fun data(): Collection<Array<Any>> =
            listOf(
                arrayOf(
                    SetCan(value = "123456".toCharArray()),
                    SetCan::class.java,
                    """{"cmd":"SET_CAN","value":"123456"}""",
                ),
                arrayOf(
                    SetCan(value = null),
                    SetCan::class.java,
                    """{"cmd":"SET_CAN"}""",
                ),
                arrayOf(
                    SetNewPin(value = "123456".toCharArray()),
                    SetNewPin::class.java,
                    """{"cmd":"SET_NEW_PIN","value":"123456"}""",
                ),
                arrayOf(
                    SetNewPin(value = null),
                    SetNewPin::class.java,
                    """{"cmd":"SET_NEW_PIN"}""",
                ),
                arrayOf(
                    SetPin(value = "123456".toCharArray()),
                    SetPin::class.java,
                    """{"cmd":"SET_PIN","value":"123456"}""",
                ),
                arrayOf(
                    SetPin(value = null),
                    SetPin::class.java,
                    """{"cmd":"SET_PIN"}""",
                ),
                arrayOf(
                    SetPuk(value = "1234567890".toCharArray()),
                    SetPuk::class.java,
                    """{"cmd":"SET_PUK","value":"1234567890"}""",
                ),
                arrayOf(
                    SetPuk(value = null),
                    SetPuk::class.java,
                    """{"cmd":"SET_PUK"}""",
                ),
            )
    }
}
