/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core

import android.content.Context
import android.net.Uri
import android.nfc.Tag
import com.governikus.ausweisapp.sdkwrapper.SDKWrapper
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Accept
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Command
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Message
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.RunAuth
import com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SetPin
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WorkflowControllerTest {
    private var workflowController: WorkflowController? = null
    private var connection: MockSdkConnection? = null

    private val mainThreadSurrogate = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        connection = MockSdkConnection()
        workflowController = WorkflowController(connection!!)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        workflowController = null
        connection = null
    }

    @Test
    fun testOnStarted() =
        runTest(timeout = 1000.milliseconds) {
            assertNotNull(workflowController)
            val workflowController = workflowController!!

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onStarted() {
                                it.resume(true)
                            }
                        },
                    )

                    workflowController.start(RuntimeEnvironment.getApplication())

                    advanceUntilIdle()
                }
            assert(completed)

            assert(workflowController.isStarted)
        }

    @Test
    fun testOnInfo() =
        runTest(timeout = 1000.milliseconds) {
            assertNotNull(workflowController)
            assertNotNull(connection)
            val workflowController = workflowController!!
            val connection = connection!!

            connection.onCommandSend = {
                val infoString = """
                {
                   "AusweisApp": "CONNECTED",
                    "VersionInfo": {
                        "Implementation-Title": "AusweisApp2",
                        "Implementation-Vendor": "Governikus GmbH & Co. KG",
                        "Implementation-Version": "2.4.104+61-default-6bb35d835157+-draft",
                        "Name": "AusweisApp2",
                        "Specification-Title": "TR-03124-1",
                        "Specification-Vendor": "Federal Office for Information Security",
                        "Specification-Version": "1.4"
                      },
                    "msg": "INFO"
                }
                """
                connection.receive(infoString)
            }

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onInfo(
                                versionInfo: VersionInfo,
                                connectionInfo: ConnectionInfo,
                            ) {
                                assertEquals(ConnectionInfo.Connected, connectionInfo)
                                it.resume(true)
                            }
                        },
                    )

                    workflowController.start(RuntimeEnvironment.getApplication())
                    assertEquals(true, workflowController.isStarted)
                    workflowController.getInfo()

                    advanceUntilIdle()
                }

            assert(completed)
        }

    @Test
    fun testErrorNotStarted() =
        runTest(timeout = 1000.milliseconds) {
            assertNotNull(workflowController)
            val workflowController = workflowController!!

            assertFalse(workflowController.isStarted)

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onWrapperError(error: WrapperError) {
                                it.resume(true)
                            }
                        },
                    )

                    workflowController.startAuthentication(Uri.parse("https://test.test"))

                    advanceUntilIdle()
                }
            assert(completed)
        }

    @Ignore("Test is flaky, kotlinx.coroutines.test.UncompletedCoroutinesError: After waiting for 1s, the test body did not run to completion")
    @Test
    fun testAuthenticationStarted() =
        runTest(timeout = 1000.milliseconds) {
            assertNotNull(workflowController)
            assertNotNull(connection)
            val workflowController = workflowController!!
            val connection = connection!!

            val testUrl = Uri.parse("https://test.test")

            connection.onCommandSend = {
                val command = it as? RunAuth

                assertNotNull(command)
                assertEquals(testUrl.toString(), command?.tcTokenURL)

                connection.receive("{\"msg\":\"AUTH\"}")
            }

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onAuthenticationStarted() {
                                it.resume(true)
                            }
                        },
                    )

                    workflowController.start(RuntimeEnvironment.getApplication())
                    workflowController.startAuthentication(testUrl)

                    advanceUntilIdle()
                }

            assert(completed)
        }

    @Test
    fun testFullAuthentication() =
        runTest(timeout = 30000.milliseconds) {
            assertNotNull(workflowController)
            assertNotNull(connection)
            val workflowController = workflowController!!
            val connection = connection!!

            val tcTokenUrl = Uri.parse("https://test.test")
            val testPin = "123456".toCharArray()

            connection.onCommandSend = { command ->
                when (command) {
                    is RunAuth -> {
                        assertNotNull(command)
                        assertEquals(tcTokenUrl.toString(), command.tcTokenURL)

                        connection.receive("{\"msg\":\"AUTH\"}")
                    }

                    is Accept -> {
                        connection.receive("{\"msg\":\"INSERT_CARD\"}")
                    }

                    is SetPin -> {
                        assertEquals(testPin, command.value)

                        connection.receive(
                            "{" +
                                "  \"msg\": \"AUTH\"," +
                                "  \"result\":" +
                                "           {" +
                                "            \"major\": \"http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok\"" +
                                "           }," +
                                "  \"url\": \"https://test.governikus-eid.de/gov_autent/async?refID=_123456789\"" +
                                "}",
                        )
                    }

                    else -> {
                    }
                }
            }

            val completed =
                suspendCoroutine<Boolean> {
                    workflowController.registerCallbacks(
                        object : TestWorkflowCallbacks() {
                            override fun onAuthenticationStarted() {
                                connection.receive(
                                    "{" +
                                        "  \"msg\": \"ACCESS_RIGHTS\"," +
                                        "  \"aux\":" +
                                        "       {" +
                                        "        \"ageVerificationDate\": \"1999-07-20\"," +
                                        "        \"requiredAge\": \"18\"," +
                                        "        \"validityDate\": \"2017-07-20\"," +
                                        "        \"communityId\": \"02760400110000\"" +
                                        "       }," +
                                        "  \"chat\":" +
                                        "        {" +
                                        "         \"effective\": [\"Address\", \"FamilyName\", \"GivenNames\", \"AgeVerification\"]," +
                                        "         \"optional\": [\"GivenNames\", \"AgeVerification\"]," +
                                        "         \"required\": [\"Address\", \"FamilyName\"]" +
                                        "        }," +
                                        "  \"transactionInfo\": \"this is an example\"" +
                                        "}",
                                )
                            }

                            override fun onAccessRights(
                                error: String?,
                                accessRights: AccessRights?,
                            ) {
                                workflowController.accept()
                            }

                            override fun onInsertCard(error: String?) {
                                connection.receive(
                                    "{" +
                                        "  \"msg\": \"READER\"," +
                                        "  \"name\": \"NFC\"," +
                                        "  \"attached\": true," +
                                        "  \"insertable\": true," +
                                        "  \"keypad\": false," +
                                        "  \"card\":" +
                                        "         {" +
                                        "          \"inoperative\": false," +
                                        "          \"deactivated\": false," +
                                        "          \"retryCounter\": 3" +
                                        "         }" +
                                        "}",
                                )
                            }

                            override fun onReader(reader: Reader?) {
                                val card = reader?.card
                                assertNotNull(card)
                                assertEquals(3, card?.pinRetryCounter)
                                assertEquals(false, card?.inoperative)
                                assertEquals(false, card?.deactivated)

                                connection.receive(
                                    "{" +
                                        "  \"msg\": \"ENTER_PIN\"," +
                                        "  \"reader\":" +
                                        "           {" +
                                        "            \"name\": \"NFC\"," +
                                        "            \"attached\": true," +
                                        "            \"insertable\": true," +
                                        "            \"keypad\": false," +
                                        "            \"card\":" +
                                        "                   {" +
                                        "                    \"inoperative\": false," +
                                        "                    \"deactivated\": false," +
                                        "                    \"retryCounter\": 3" +
                                        "                   }" +
                                        "           }" +
                                        "}",
                                )
                            }

                            override fun onEnterPin(
                                error: String?,
                                reader: Reader,
                            ) {
                                val card = reader.card
                                if (card != null) {
                                    assertEquals(3, card.pinRetryCounter)
                                    assertEquals(false, card.inoperative)
                                    assertEquals(false, card.deactivated)

                                    workflowController.setPin(testPin)
                                } else {
                                    assert(false)
                                }
                            }

                            override fun onAuthenticationCompleted(authResult: AuthResult) {
                                assertNotNull(authResult.result)
                                assertNotNull(authResult.url)
                                assertEquals("http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok", authResult.result?.major)

                                it.resume(true)
                            }
                        },
                    )

                    workflowController.start(RuntimeEnvironment.getApplication())
                    workflowController.startAuthentication(tcTokenUrl)

                    advanceUntilIdle()
                }

            assert(completed)
        }
}

internal class MockSdkConnection : WorkflowController.SdkConnection {
    private var onMessageReceived: ((message: Message) -> Unit)? = null
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    var onCommandSend: ((command: Command) -> Unit)? = null

    override var isConnected: Boolean = false
        private set

    override fun bind(
        context: Context,
        onConnected: (() -> Unit)?,
        onConnectionFailed: (() -> Unit)?,
        onMessageReceived: ((message: Message) -> Unit)?,
    ) {
        this.onMessageReceived = onMessageReceived
        isConnected = true
        onConnected?.invoke()
    }

    override fun unbind() {
        onMessageReceived = null
        isConnected = false
    }

    override fun updateNfcTag(tag: Tag): Boolean = false

    override fun <T : Command> send(
        command: T,
        clazz: Class<T>,
    ): Boolean {
        SDKWrapper.launch {
            onCommandSend?.invoke(command)
        }
        return isConnected
    }

    fun receive(messageJson: String) {
        SDKWrapper.launch {
            moshi.adapter(Message::class.java).fromJson(messageJson)?.let {
                onMessageReceived?.invoke(it)
            }
        }
    }
}

internal open class TestWorkflowCallbacks : WorkflowCallbacks {
    override fun onStarted() {}

    override fun onAuthenticationStarted() {}

    override fun onAuthenticationStartFailed(error: String) {}

    override fun onChangePinStarted() {}

    override fun onAccessRights(
        error: String?,
        accessRights: AccessRights?,
    ) {}

    override fun onCertificate(certificateDescription: CertificateDescription) {}

    override fun onInsertCard(error: String?) {}

    override fun onReader(reader: Reader?) {}

    override fun onReaderList(readers: List<Reader>?) {}

    override fun onEnterPin(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterNewPin(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterPuk(
        error: String?,
        reader: Reader,
    ) {}

    override fun onEnterCan(
        error: String?,
        reader: Reader,
    ) {}

    override fun onAuthenticationCompleted(authResult: AuthResult) {}

    override fun onChangePinCompleted(changePinResult: ChangePinResult) {}

    override fun onWrapperError(error: WrapperError) {}

    override fun onStatus(workflowProgress: WorkflowProgress) {}

    override fun onInfo(
        versionInfo: VersionInfo,
        connectionInfo: ConnectionInfo,
    ) {}

    override fun onBadState(error: String) {}

    override fun onInternalError(error: String) {}

    override fun onPause(cause: Cause) {}
}
