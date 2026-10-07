/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.governikus.ausweisapp.sdkwrapper.SDKWrapper.workflowController
import com.governikus.ausweisapp.sdkwrapper.card.core.AccessRight
import com.governikus.ausweisapp.sdkwrapper.card.core.AccessRights
import com.governikus.ausweisapp.sdkwrapper.card.core.AuthResult
import com.governikus.ausweisapp.sdkwrapper.card.core.Card
import com.governikus.ausweisapp.sdkwrapper.card.core.Cause
import com.governikus.ausweisapp.sdkwrapper.card.core.CertificateDescription
import com.governikus.ausweisapp.sdkwrapper.card.core.ChangePinResult
import com.governikus.ausweisapp.sdkwrapper.card.core.ConnectionInfo
import com.governikus.ausweisapp.sdkwrapper.card.core.Reader
import com.governikus.ausweisapp.sdkwrapper.card.core.Simulator
import com.governikus.ausweisapp.sdkwrapper.card.core.SimulatorFile
import com.governikus.ausweisapp.sdkwrapper.card.core.SimulatorKey
import com.governikus.ausweisapp.sdkwrapper.card.core.VersionInfo
import com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowCallbacks
import com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowProgress
import com.governikus.ausweisapp.sdkwrapper.card.core.WrapperError
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.Authentication.Companion.RESULT_AUTH
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.Authentication.SimulatorMode
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.ChangePin.Companion.RESULT_CHANGE_PIN
import com.governikus.ausweisapp.tester.wrapper.R
import com.governikus.ausweisapp.tester.wrapper.card.ui.password.EnterPasswordFragment
import com.governikus.ausweisapp.tester.wrapper.common.NavLiveEvent
import com.governikus.ausweisapp.tester.wrapper.common.ToastLiveEvent
import com.governikus.ausweisapp.tester.wrapper.common.WorkflowLiveEvent
import com.governikus.ausweisapp.tester.wrapper.common.finished
import com.governikus.ausweisapp.tester.wrapper.common.navigate
import com.governikus.ausweisapp.tester.wrapper.common.show

internal class WorkflowViewModel(
    application: Application,
) : AndroidViewModel(application) {
    enum class WorkflowStatus {
        INITIAL,
        STARTED,
        CANCELLED,
        COMPLETED,
    }

    val navigation = NavLiveEvent()
    val workflowEvent = WorkflowLiveEvent()
    val toast = ToastLiveEvent()

    var tcTokenUrl: Uri? = null
    var developerMode: Boolean = false
    var cardSimulatorMode: SimulatorMode = SimulatorMode.DISABLED

    private var pin: CharArray? = null
    private var can: CharArray? = null
    private var puk: CharArray? = null
    private var newPin: CharArray? = null

    val hasStoredPin: Boolean
        get() = pin != null

    val hasStoredCan: Boolean
        get() = can != null

    val accessRights = MutableLiveData<AccessRights>()
    val certificateDescription = MutableLiveData<CertificateDescription>()

    val errorMessage = MutableLiveData<String>()

    val currentCard = MutableLiveData<Card>()
    val lastCard = MutableLiveData<Card>()
    val connectedReaders = mutableMapOf<String, Reader>()
    val hasPinPadReader: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }

    val workflowProgress = MutableLiveData<WorkflowProgress>()

    var workflowStatus = WorkflowStatus.INITIAL
    private var didRequestPassword = false

    lateinit var workflow: WorkflowActivity.Workflow

    private var authResult: AuthResult? = null
    private var changePinResult: ChangePinResult? = null

    private fun initSimulatorFiles() =
        listOf(
            SimulatorFile(fileId = "0101", shortFileId = "01", content = "610413024944"),
            SimulatorFile(fileId = "0102", shortFileId = "02", content = "6203130144"),
            SimulatorFile(fileId = "0103", shortFileId = "03", content = "630a12083230323931303331"),
            SimulatorFile(fileId = "0104", shortFileId = "04", content = "64070c054552494b41"),
            SimulatorFile(
                fileId = "0105",
                shortFileId = "05",
                content = "650c0c0a4d55535445524d414e4e",
            ),
            SimulatorFile(fileId = "0106", shortFileId = "06", content = "66020c00"),
            SimulatorFile(fileId = "0107", shortFileId = "07", content = "67020c00"),
            SimulatorFile(fileId = "0108", shortFileId = "08", content = "680a12083139363430383132"),
            SimulatorFile(fileId = "0109", shortFileId = "09", content = "690aa1080c064245524c494e"),
            SimulatorFile(fileId = "010a", shortFileId = "0a", content = "6a03130144"),
            SimulatorFile(fileId = "010b", shortFileId = "0b", content = "6b03130146"),
            SimulatorFile(
                fileId = "010c",
                shortFileId = "0c",
                content = "6c30312e302c06072a8648ce3d0101022100a9fb57dba1eea9bc3e660a909d838d726e3bf623d52620282013481d1f6e5377",
            ),
            SimulatorFile(fileId = "010d", shortFileId = "0d", content = "6d080c064741424c4552"),
            SimulatorFile(fileId = "010f", shortFileId = "0f", content = "6f0a12083230313931313031"),
            SimulatorFile(
                fileId = "0111",
                shortFileId = "11",
                content = "712d302baa120c10484549444553545241e1ba9e45203137ab070c054bc3964c4ead03130144ae0713053531313437",
            ),
            SimulatorFile(fileId = "0112", shortFileId = "12", content = "7209040702760503150000"),
            SimulatorFile(
                fileId = "0113",
                shortFileId = "13",
                content = "7316a1140c125245534944454e4345205045524d49542031",
            ),
            SimulatorFile(
                fileId = "0114",
                shortFileId = "14",
                content = "7416a1140c125245534944454e4345205045524d49542032",
            ),
            SimulatorFile(
                fileId = "0115",
                shortFileId = "15",
                content = "7515131374656c3a2b34392d3033302d31323334353637",
            ),
            SimulatorFile(
                fileId = "0116",
                shortFileId = "16",
                content = "761516136572696b61406d75737465726d616e6e2e6465",
            ),
        )

    private fun replaceSimulatorFile(
        files: MutableList<SimulatorFile>,
        updated: SimulatorFile,
    ) {
        val index = files.indexOf(element = updated)
        if (index != -1) {
            files[index] = updated
        }
    }

    private val workflowCallback =
        object : WorkflowCallbacks {
            override fun onStarted() {
                workflowController.getInfo()
                workflowController.getReader(name = "Simulator")
                workflowController.getReaderList()
                if (workflow == WorkflowActivity.Workflow.AUTHENTICATE) {
                    startAuthentication()
                }
            }

            override fun onAuthenticationStarted() {
                workflowStatus = WorkflowStatus.STARTED
            }

            override fun onAuthenticationStartFailed(error: String) {
                println(message = "AUTH_START_FAILED: The Authentication start failed with the following message: $error")
                errorMessage.value = error
                navigation.navigate(action = R.id.action_error_occured)
            }

            override fun onChangePinStarted() {
                workflowStatus = WorkflowStatus.STARTED
            }

            override fun onAccessRights(
                error: String?,
                accessRights: AccessRights?,
            ) {
                if (showErrorMessageIfError(error = error)) return

                val currentRights = this@WorkflowViewModel.accessRights.value
                this@WorkflowViewModel.accessRights.value = accessRights

                // Only handle the first request, every other request is just an update of the rights
                if (currentRights != null) {
                    return
                }

                workflowController.getCertificate()
                navigation.navigate(action = R.id.action_request_access_rights)
            }

            override fun onCertificate(certificateDescription: CertificateDescription) {
                this@WorkflowViewModel.certificateDescription.value = certificateDescription
            }

            override fun onPause(cause: Cause) {
                this@WorkflowViewModel.errorMessage.value = cause.rawName
                navigation.navigate(action = R.id.action_pause)
            }

            override fun onReader(reader: Reader?) {
                println(
                    message =
                        "Received READER\n" +
                            "The current name is: ${reader?.name}\n" +
                            "The current attached state is: ${reader?.attached}\n" +
                            "The current keypad stater is: ${reader?.keypad}\n" +
                            "The current insertable state is: ${reader?.insertable}\n",
                )

                val attached: Boolean = reader?.attached ?: return
                if (attached) {
                    connectedReaders[reader.name] = reader
                } else {
                    connectedReaders.remove(key = reader.name)
                }

                hasPinPadReader.value = connectedReaders.values.any { it.keypad && it.name != "Simulator" }

                val card: Card = reader.card ?: return

                if (card.isUnknown()) {
                    toast.show(text = application.getString(R.string.card_workflow_unknown_card))
                    return
                }

                if (card.deactivated == true) {
                    toast.show(text = application.getString(R.string.card_workflow_card_deactivated))
                    return
                }
                if (card.inoperative == true) {
                    toast.show(text = application.getString(R.string.card_workflow_card_inoperative))
                    return
                }
                // Only navigate to the recognized view, if a card was recognized and we are on the request card view.
                // Otherwise we might accidentally move to it, when we detect a card while the user does something else
                if (navigation.value?.peekContent()?.action == R.id.action_card_requested) {
                    navigation.navigate(action = R.id.action_card_recognized)
                }
            }

            override fun onReaderList(readers: List<Reader>?) {
                println(message = "GET_READER_LIST: Start of callback")
                if (readers != null) {
                    println(message = "Received READER list from GET_READER_LIST")
                    for (reader in readers) {
                        println(
                            message =
                                "Reader name is: ${reader.name}\n" +
                                    "Reader attached state is: ${reader.attached}\n" +
                                    "Reader keypad stater is: ${reader.keypad}\n" +
                                    "Reader insertable state is: ${reader.insertable}\n",
                        )
                    }
                }
                println(message = "GET_READER_LIST: End of callback")
            }

            override fun onInsertCard(error: String?) {
                if (showErrorMessageIfError(error = error)) return

                when (cardSimulatorMode) {
                    SimulatorMode.DEFAULT_DATA -> {
                        workflowController.setCard(name = "Simulator", simulator = null)
                    }

                    SimulatorMode.DIFFERENT_FIRST_NAME -> {
                        val simulatorFiles = initSimulatorFiles()
                        replaceSimulatorFile(
                            files = simulatorFiles as MutableList<SimulatorFile>,
                            updated =
                                SimulatorFile(
                                    fileId = "0104",
                                    shortFileId = "04",
                                    content = "64060c044552494b",
                                ),
                        ) // ERIK
                        workflowController.setCard(
                            name = "Simulator",
                            simulator =
                                Simulator(
                                    files = simulatorFiles,
                                    keys = null,
                                ),
                        )
                    }

                    SimulatorMode.DIFFERENT_PSEUDONYM -> {
                        val simulator =
                            Simulator(
                                files = initSimulatorFiles(),
                                keys =
                                    listOf(
                                        element =
                                            SimulatorKey(
                                                id = 2,
                                                content = "308201610201003081ec06072a8648ce3d02013081e0020101302c06072a8648ce3d0101022100a9fb57dba1eea9bc3e660a909d838d726e3bf623d52620282013481d1f6e5377304404207d5a0975fc2c3057eef67530417affe7fb8055c126dc5c6ce94a4b44f330b5d9042026dc5c6ce94a4b44f330b5d9bbd77cbf958416295cf7e1ce6bccdc18ff8c07b60441048bd2aeb9cb7e57cb2c4b482ffc81b7afb9de27e1e3bd23c23a4453bd9ace3262547ef835c3dac4fd97f8461a14611dc9c27745132ded8e545c1d54c72f046997022100a9fb57dba1eea9bc3e660a909d838d718c397aa3b561a6f7901e0e82974856a7020101046d306b020101042005eefab8d4e0bb6a0db1e587ddc81838546cab90013ab95186a1033116526af2a144034200046e5e1c5f6b36b4b5ce6a82d71c753fdc6bb0efc7a93c4ac71201e05f5b77c2a274d50e134ec6f362f93eed7c1b81abd7c187df60aab6c2a726b6e62e39d4aa9f",
                                            ),
                                    ),
                            )
                        workflowController.setCard(name = "Simulator", simulator = simulator)
                    }

                    else -> {
                        navigation.navigate(action = R.id.action_card_requested)
                    }
                }
            }

            override fun onEnterPin(
                error: String?,
                reader: Reader,
            ) {
                if (showErrorMessageIfError(error = error)) return
                val card = reader.card ?: return
                if (card.isUnknown()) return

                currentCard.value = card
                if (reader.keypad) {
                    workflowController.setPin(pin = null)
                    return
                }

                val currentPin = pin
                pin = null
                if (currentPin == null || currentPin.size == 0) {
                    didRequestPassword = true
                    if (workflow == WorkflowActivity.Workflow.CHANGE_TRANSPORT_PIN) {
                        navigation.navigate(
                            action = R.id.action_request_pin,
                            data =
                                Bundle().apply {
                                    putString(
                                        "passwordType",
                                        EnterPasswordFragment.PasswordType.TRANSPORT_PIN.type,
                                    )
                                },
                        )
                    } else {
                        navigation.navigate(action = R.id.action_request_pin)
                    }
                } else {
                    workflowController.setPin(pin = currentPin)
                }
            }

            override fun onEnterNewPin(
                error: String?,
                reader: Reader,
            ) {
                if (showErrorMessageIfError(error = error)) return
                val card = reader.card ?: return
                if (card.isUnknown()) return

                currentCard.value = card
                if (reader.keypad) {
                    workflowController.setNewPin(newPin = null)
                    return
                }

                val currentNewPin = newPin
                newPin = null
                if (currentNewPin == null || currentNewPin.size == 0) {
                    didRequestPassword = true
                    navigation.navigate(action = R.id.action_request_new_pin)
                } else {
                    workflowController.setNewPin(newPin = currentNewPin)
                }
            }

            override fun onEnterPuk(
                error: String?,
                reader: Reader,
            ) {
                if (showErrorMessageIfError(error = error)) return
                val card = reader.card ?: return
                if (card.isUnknown()) return

                currentCard.value = card
                if (reader.keypad) {
                    workflowController.setPuk(puk = null)
                    return
                }

                val currentPuk = puk
                puk = null
                if (currentPuk == null || currentPuk.size == 0) {
                    didRequestPassword = true
                    navigation.navigate(action = R.id.action_request_puk)
                } else {
                    workflowController.setPuk(puk = currentPuk)
                }
            }

            override fun onEnterCan(
                error: String?,
                reader: Reader,
            ) {
                if (showErrorMessageIfError(error = error)) return
                val card = reader.card ?: return
                if (card.isUnknown()) return

                currentCard.value = card
                if (reader.keypad) {
                    workflowController.setCan(can = null)
                    return
                }

                val currentCan = can
                can = null
                if (currentCan == null || currentCan.size == 0) {
                    didRequestPassword = true
                    navigation.navigate(action = R.id.action_request_can)
                } else {
                    workflowController.setCan(can = currentCan)
                }
            }

            override fun onAuthenticationCompleted(authResult: AuthResult) {
                workflowStatus = WorkflowStatus.COMPLETED
                this@WorkflowViewModel.authResult = authResult

                val isError = authResult.result?.major?.contains(other = "resultmajor#error") == true
                val isCancellationByUser = authResult.result?.minor?.endsWith(suffix = "cancellationByUser") == true

                when {
                    isCancellationByUser -> {
                        finishWithResult()
                    }

                    !isError -> {
                        toast.show(text = application.getString(R.string.card_workflow_authentication_finished_remove_card_message))
                        finishWithResult()
                    }

                    else -> {
                        val authErrorMessage = authResult.result?.message
                        errorMessage.value =
                            if (authErrorMessage.isNullOrBlank()) {
                                application.getString(
                                    R.string.error_message_unknown_error,
                                )
                            } else {
                                authErrorMessage
                            }
                        navigation.navigate(action = R.id.action_error_occured)
                    }
                }
            }

            override fun onChangePinCompleted(changePinResult: ChangePinResult) {
                workflowStatus = WorkflowStatus.COMPLETED
                toast.show(text = application.getString(R.string.card_workflow_pin_finished_remove_card_message))
                if (changePinResult.success) {
                    toast.show(text = application.getString(R.string.change_pin_result_true))
                } else {
                    toast.show(text = application.getString(R.string.change_pin_result_false, changePinResult.reason))
                }
                this@WorkflowViewModel.changePinResult = changePinResult

                if (navigation.value?.peekContent()?.action != R.id.error) {
                    finishWithResult()
                }
            }

            override fun onWrapperError(error: WrapperError) {
                // Not implemented by the SDKTester yet.
            }

            override fun onStatus(workflowProgress: WorkflowProgress) {
                this@WorkflowViewModel.workflowProgress.value = workflowProgress
            }

            override fun onInfo(
                versionInfo: VersionInfo,
                connectionInfo: ConnectionInfo,
            ) {
                println(
                    message =
                        "Received INFO from GET_INFO\n" +
                            "The current name is: ${versionInfo.name}\n" +
                            "The current implementationTittle is: ${versionInfo.implementationTitle}\n" +
                            "The current implementationVendor is: ${versionInfo.implementationVendor}\n" +
                            "The current specificationVendor is: ${versionInfo.specificationVendor}\n" +
                            "The current specificationVersion is: ${versionInfo.specificationVersion}\n" +
                            "The current state of LocalIfd is: ${connectionInfo}\n",
                )
            }

            override fun onBadState(error: String) {
                println(message = "An BAD_STATE of the AusweisApp SDK occured: $error")
            }

            override fun onInternalError(error: String) {
                println(message = "An INTERNAL_ERROR of the AusweisApp SDK occured: $error")
                showErrorMessageIfError(error = error)
            }
        }

    init {
        workflowController.registerCallbacks(callbacks = workflowCallback)
        workflowController.start(context = application)
    }

    override fun onCleared() {
        workflowController.unregisterCallbacks(callbacks = workflowCallback)
        workflowController.stop()
    }

    private fun startAuthentication() {
        val tcTokenUrl = tcTokenUrl ?: error(message = "Missing tcTokenUrl")
        workflowController.startAuthentication(
            tcTokenUrl = tcTokenUrl,
            developerMode = developerMode,
            header = hashMapOf("Bearer" to "0123456789abcdef"),
        )
    }

    private fun startChangePin() {
        workflowController.startChangePin()
    }

    fun setPin(pin: CharArray?) {
        lastCard.value = currentCard.value

        navigation.navigate(action = R.id.password_entered)
        if (didRequestPassword) {
            workflowController.setPin(pin = pin)
        } else {
            this.pin = pin
            if (workflow == WorkflowActivity.Workflow.AUTHENTICATE) {
                workflowController.accept()
            }
        }
    }

    fun setCan(can: CharArray?) {
        lastCard.value = currentCard.value

        navigation.navigate(action = R.id.password_entered)
        if (didRequestPassword) {
            workflowController.setCan(can = can)
        } else {
            this.can = can
            workflowController.accept()
        }
    }

    fun setPuk(puk: CharArray?) {
        lastCard.value = currentCard.value

        navigation.navigate(action = R.id.password_entered)
        workflowController.setPuk(puk = puk)
    }

    fun setNewPin(newPin: CharArray?) {
        lastCard.value = currentCard.value

        navigation.navigate(action = R.id.password_entered)
        if (didRequestPassword) {
            workflowController.setNewPin(newPin = newPin)
        } else {
            this.newPin = newPin
            startChangePin()
        }
    }

    fun acceptAccessRights(acceptedOptionalRights: List<AccessRight>) {
        if (acceptedOptionalRights.isNotEmpty()) {
            workflowController.setAccessRights(accessRights = acceptedOptionalRights)
        }

        println(message = "Getting current GET_ACCESS_RIGHTS as a test.")
        workflowController.getAccessRights()

        val can = can
        val pin = pin
        val isCanAllowed = acceptedOptionalRights.contains(element = AccessRight.CAN_ALLOWED)
        when {
            cardSimulatorMode != SimulatorMode.DISABLED -> runWithCardSimulator()
            isCanAllowed && can != null -> setCan(can = can)
            pin != null -> setPin(pin = pin)
            isCanAllowed -> navigation.navigate(action = R.id.action_request_can)
            else -> navigation.navigate(action = R.id.action_request_pin)
        }
    }

    private fun runWithCardSimulator() {
        navigation.navigate(action = R.id.action_request_pin)
        navigation.navigate(action = R.id.password_entered)
        workflowController.setPin(pin = null)
        if (workflow == WorkflowActivity.Workflow.AUTHENTICATE) {
            workflowController.accept()
        }
    }

    fun continueWorkflow() {
        this@WorkflowViewModel.errorMessage.value = null
        navigation.navigate(action = R.id.action_continue_reading)
        workflowController.continueWorkflow()
    }

    fun acceptError() {
        finishWithResult()
    }

    private fun finishWithResult() {
        when (workflow) {
            WorkflowActivity.Workflow.AUTHENTICATE -> {
                val result =
                    Bundle().apply {
                        putParcelable(
                            RESULT_AUTH,
                            authResult,
                        )
                    }
                workflowEvent.finished(resultCode = Activity.RESULT_OK, data = result)
            }

            WorkflowActivity.Workflow.CHANGE_PIN, WorkflowActivity.Workflow.CHANGE_TRANSPORT_PIN -> {
                val result =
                    Bundle().apply {
                        putParcelable(
                            RESULT_CHANGE_PIN,
                            changePinResult,
                        )
                    }
                workflowEvent.finished(resultCode = Activity.RESULT_OK, data = result)
            }
        }
    }

    fun showCertificate() {
        navigation.navigate(action = R.id.action_show_certificate)
    }

    fun cancelWorkflow() {
        if (workflowStatus == WorkflowStatus.INITIAL) {
            finishWithResult()
            return
        }

        if (workflowStatus != WorkflowStatus.STARTED) {
            return
        }

        workflowStatus = WorkflowStatus.CANCELLED
        workflowController.cancel()

        if (workflow == WorkflowActivity.Workflow.AUTHENTICATE) {
            navigation.navigate(action = R.id.action_authentication_aborted)
        }
    }

    fun presetPin(pin: CharArray?) {
        this.pin = pin
    }

    fun presetCan(can: CharArray?) {
        this.can = can
    }

    private fun showErrorMessageIfError(error: String?): Boolean {
        val err = error.orEmpty()
        if (error != null) {
            errorMessage.value = err
            navigation.navigate(action = R.id.action_error_occured)
            return true
        }
        return false
    }
}
