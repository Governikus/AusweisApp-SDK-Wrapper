/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
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
    var connectedReaders = mutableMapOf<String, Reader>()
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
            SimulatorFile("0101", "01", "610413024944"),
            SimulatorFile("0102", "02", "6203130144"),
            SimulatorFile("0103", "03", "630a12083230323931303331"),
            SimulatorFile("0104", "04", "64070c054552494b41"),
            SimulatorFile("0105", "05", "650c0c0a4d55535445524d414e4e"),
            SimulatorFile("0106", "06", "66020c00"),
            SimulatorFile("0107", "07", "67020c00"),
            SimulatorFile("0108", "08", "680a12083139363430383132"),
            SimulatorFile("0109", "09", "690aa1080c064245524c494e"),
            SimulatorFile("010a", "0a", "6a03130144"),
            SimulatorFile("010b", "0b", "6b03130146"),
            SimulatorFile(
                "010c",
                "0c",
                "6c30312e302c06072a8648ce3d0101022100a9fb57dba1eea9bc3e660a909d838d726e3bf623d52620282013481d1f6e5377",
            ),
            SimulatorFile("010d", "0d", "6d080c064741424c4552"),
            SimulatorFile("010f", "0f", "6f0a12083230313931313031"),
            SimulatorFile(
                "0111",
                "11",
                "712d302baa120c10484549444553545241e1ba9e45203137ab070c054bc3964c4ead03130144ae0713053531313437",
            ),
            SimulatorFile("0112", "12", "7209040702760503150000"),
            SimulatorFile("0113", "13", "7316a1140c125245534944454e4345205045524d49542031"),
            SimulatorFile("0114", "14", "7416a1140c125245534944454e4345205045524d49542032"),
            SimulatorFile("0115", "15", "7515131374656c3a2b34392d3033302d31323334353637"),
            SimulatorFile("0116", "16", "761516136572696b61406d75737465726d616e6e2e6465"),
        )

    private fun replaceSimulatorFile(
        files: MutableList<SimulatorFile>,
        updated: SimulatorFile,
    ) {
        val index = files.indexOf(updated)
        if (index != -1) {
            files[index] = updated
        }
    }

    private val workflowCallback =
        object : WorkflowCallbacks {
            override fun onStarted() {
                workflowController.getInfo()
                workflowController.getReader("Simulator")
                workflowController.getReaderList()
                if (workflow == WorkflowActivity.Workflow.AUTHENTICATE) {
                    startAuthentication()
                }
            }

            override fun onAuthenticationStarted() {
                workflowStatus = WorkflowStatus.STARTED
            }

            override fun onAuthenticationStartFailed(error: String) {
                println("AUTH_START_FAILED: The Authentication start failed with the following message: $error")
                errorMessage.value = error
                navigation.navigate(R.id.action_error_occured)
            }

            override fun onChangePinStarted() {
                workflowStatus = WorkflowStatus.STARTED
            }

            override fun onAccessRights(
                error: String?,
                accessRights: AccessRights?,
            ) {
                if (showErrorMessageIfError(error)) return

                val currentRights = this@WorkflowViewModel.accessRights.value
                this@WorkflowViewModel.accessRights.value = accessRights

                // Only handle the first request, every other request is just an update of the rights
                if (currentRights != null) {
                    return
                }

                workflowController.getCertificate()
                navigation.navigate(R.id.action_request_access_rights)
            }

            override fun onCertificate(certificateDescription: CertificateDescription) {
                this@WorkflowViewModel.certificateDescription.value = certificateDescription
            }

            override fun onPause(cause: Cause) {
                this@WorkflowViewModel.errorMessage.value = cause.rawName
                navigation.navigate(R.id.action_pause)
            }

            override fun onReader(reader: Reader?) {
                println(
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
                    connectedReaders.remove(reader.name)
                }

                hasPinPadReader.value = connectedReaders.values.any { it.keypad && it.name != "Simulator" }

                val card: Card = reader.card ?: return

                if (card.isUnknown()) {
                    toast.show(application.getString(R.string.card_workflow_unknown_card))
                    return
                }

                if (card.deactivated == true) {
                    toast.show(application.getString(R.string.card_workflow_card_deactivated))
                    return
                }
                if (card.inoperative == true) {
                    toast.show(application.getString(R.string.card_workflow_card_inoperative))
                    return
                }
                // Only navigate to the recognized view, if a card was recognized and we are on the request card view.
                // Otherwise we might accidentally move to it, when we detect a card while the user does something else
                if (navigation.value?.peekContent()?.action == R.id.action_card_requested) {
                    navigation.navigate(R.id.action_card_recognized)
                }
            }

            override fun onReaderList(readers: List<Reader>?) {
                println("GET_READER_LIST: Start of callback")
                if (readers != null) {
                    println("Received READER list from GET_READER_LIST")
                    for (reader in readers) {
                        println(
                            "Reader name is: ${reader.name}\n" +
                                "Reader attached state is: ${reader.attached}\n" +
                                "Reader keypad stater is: ${reader.keypad}\n" +
                                "Reader insertable state is: ${reader.insertable}\n",
                        )
                    }
                }
                println("GET_READER_LIST: End of callback")
            }

            override fun onInsertCard(error: String?) {
                if (showErrorMessageIfError(error)) return

                when (cardSimulatorMode) {
                    SimulatorMode.DEFAULT_DATA -> {
                        workflowController.setCard("Simulator", null)
                    }

                    SimulatorMode.DIFFERENT_FIRST_NAME -> {
                        val simulatorFiles = initSimulatorFiles()
                        replaceSimulatorFile(simulatorFiles as MutableList<SimulatorFile>, SimulatorFile("0104", "04", "64060c044552494b")) // ERIK
                        workflowController.setCard("Simulator", Simulator(simulatorFiles, null))
                    }

                    SimulatorMode.DIFFERENT_PSEUDONYM -> {
                        val simulator =
                            Simulator(
                                initSimulatorFiles(),
                                listOf(
                                    SimulatorKey(
                                        2,
                                        "308201610201003081ec06072a8648ce3d02013081e0020101302c06072a8648ce3d0101022100a9fb57dba1eea9bc3e660a909d838d726e3bf623d52620282013481d1f6e5377304404207d5a0975fc2c3057eef67530417affe7fb8055c126dc5c6ce94a4b44f330b5d9042026dc5c6ce94a4b44f330b5d9bbd77cbf958416295cf7e1ce6bccdc18ff8c07b60441048bd2aeb9cb7e57cb2c4b482ffc81b7afb9de27e1e3bd23c23a4453bd9ace3262547ef835c3dac4fd97f8461a14611dc9c27745132ded8e545c1d54c72f046997022100a9fb57dba1eea9bc3e660a909d838d718c397aa3b561a6f7901e0e82974856a7020101046d306b020101042005eefab8d4e0bb6a0db1e587ddc81838546cab90013ab95186a1033116526af2a144034200046e5e1c5f6b36b4b5ce6a82d71c753fdc6bb0efc7a93c4ac71201e05f5b77c2a274d50e134ec6f362f93eed7c1b81abd7c187df60aab6c2a726b6e62e39d4aa9f",
                                    ),
                                ),
                            )
                        workflowController.setCard("Simulator", simulator)
                    }

                    else -> {
                        navigation.navigate(R.id.action_card_requested)
                    }
                }
            }

            override fun onEnterPin(
                error: String?,
                reader: Reader,
            ) {
                if (showErrorMessageIfError(error)) return
                if (reader.card == null) return
                if (reader.card!!.isUnknown()) return

                currentCard.value = reader.card!!
                if (reader.keypad) {
                    workflowController.setPin(null)
                    return
                }

                val currentPin = pin
                pin = null
                if (currentPin == null || currentPin.size == 0) {
                    didRequestPassword = true
                    if (workflow == WorkflowActivity.Workflow.CHANGE_TRANSPORT_PIN) {
                        navigation.navigate(
                            R.id.action_request_pin,
                            Bundle().apply {
                                putString(
                                    "passwordType",
                                    EnterPasswordFragment.PasswordType.TRANSPORT_PIN.type,
                                )
                            },
                        )
                    } else {
                        navigation.navigate(R.id.action_request_pin)
                    }
                } else {
                    workflowController.setPin(currentPin)
                }
            }

            override fun onEnterNewPin(
                error: String?,
                reader: Reader,
            ) {
                if (showErrorMessageIfError(error)) return
                if (reader.card == null) return
                if (reader.card!!.isUnknown()) return

                currentCard.value = reader.card!!
                if (reader.keypad) {
                    workflowController.setNewPin(null)
                    return
                }

                val currentNewPin = newPin
                newPin = null
                if (currentNewPin == null || currentNewPin.size == 0) {
                    didRequestPassword = true
                    navigation.navigate(R.id.action_request_new_pin)
                } else {
                    workflowController.setNewPin(currentNewPin)
                }
            }

            override fun onEnterPuk(
                error: String?,
                reader: Reader,
            ) {
                if (showErrorMessageIfError(error)) return
                if (reader.card == null) return
                if (reader.card!!.isUnknown()) return

                currentCard.value = reader.card!!
                if (reader.keypad) {
                    workflowController.setPuk(null)
                    return
                }

                val currentPuk = puk
                puk = null
                if (currentPuk == null || currentPuk.size == 0) {
                    didRequestPassword = true
                    navigation.navigate(R.id.action_request_puk)
                } else {
                    workflowController.setPuk(currentPuk)
                }
            }

            override fun onEnterCan(
                error: String?,
                reader: Reader,
            ) {
                if (showErrorMessageIfError(error)) return
                if (reader.card == null) return
                if (reader.card!!.isUnknown()) return

                currentCard.value = reader.card!!
                if (reader.keypad) {
                    workflowController.setCan(null)
                    return
                }

                val currentCan = can
                can = null
                if (currentCan == null || currentCan.size == 0) {
                    didRequestPassword = true
                    navigation.navigate(R.id.action_request_can)
                } else {
                    workflowController.setCan(currentCan)
                }
            }

            override fun onAuthenticationCompleted(authResult: AuthResult) {
                workflowStatus = WorkflowStatus.COMPLETED
                this@WorkflowViewModel.authResult = authResult

                val isError = authResult.result?.major?.contains("resultmajor#error") == true
                val isCancellationByUser = authResult.result?.minor?.endsWith("cancellationByUser") == true

                when {
                    isCancellationByUser -> {
                        finishWithResult()
                    }

                    !isError -> {
                        toast.show(application.getString(R.string.card_workflow_authentication_finished_remove_card_message))
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
                        navigation.navigate(R.id.action_error_occured)
                    }
                }
            }

            override fun onChangePinCompleted(changePinResult: ChangePinResult) {
                workflowStatus = WorkflowStatus.COMPLETED
                toast.show(application.getString(R.string.card_workflow_pin_finished_remove_card_message))

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
                println("An BAD_STATE of the AusweisApp2 SDK occured: $error")
            }

            override fun onInternalError(error: String) {
                println("An INTERNAL_ERROR of the AusweisApp2 SDK occured: $error")
                showErrorMessageIfError(error)
            }
        }

    init {
        workflowController.registerCallbacks(workflowCallback)
        workflowController.start(application)
    }

    override fun onCleared() {
        super.onCleared()
        workflowController.unregisterCallbacks(workflowCallback)
        workflowController.stop()
    }

    private fun startAuthentication() {
        val tcTokenUrl = tcTokenUrl ?: throw IllegalStateException("Missing tcTokenUrl")
        workflowController.startAuthentication(tcTokenUrl, developerMode)
    }

    private fun startChangePin() {
        workflowController.startChangePin()
    }

    fun setPin(pin: CharArray?) {
        lastCard.value = currentCard.value

        navigation.navigate(R.id.password_entered)
        if (didRequestPassword) {
            workflowController.setPin(pin)
        } else {
            this.pin = pin
            if (workflow == WorkflowActivity.Workflow.AUTHENTICATE) {
                workflowController.accept()
            }
        }
    }

    fun setCan(can: CharArray?) {
        lastCard.value = currentCard.value

        navigation.navigate(R.id.password_entered)
        if (didRequestPassword) {
            workflowController.setCan(can)
        } else {
            this.can = can
            workflowController.accept()
        }
    }

    fun setPuk(puk: CharArray?) {
        lastCard.value = currentCard.value

        navigation.navigate(R.id.password_entered)
        workflowController.setPuk(puk)
    }

    fun setNewPin(newPin: CharArray?) {
        lastCard.value = currentCard.value

        navigation.navigate(R.id.password_entered)
        if (didRequestPassword) {
            workflowController.setNewPin(newPin)
        } else {
            this.newPin = newPin
            startChangePin()
        }
    }

    fun acceptAccessRights(acceptedOptionalRights: List<AccessRight>) {
        if (!acceptedOptionalRights.isEmpty()) {
            workflowController.setAccessRights(acceptedOptionalRights)
        }

        println("Getting current GET_ACCESS_RIGHTS as a test.")
        workflowController.getAccessRights()

        val can = can
        val pin = pin
        val isCanAllowed = acceptedOptionalRights.contains(AccessRight.CAN_ALLOWED)
        when {
            cardSimulatorMode != SimulatorMode.DISABLED -> runWithCardSimulator()
            isCanAllowed && can != null -> setCan(can)
            pin != null -> setPin(pin)
            isCanAllowed -> navigation.navigate(R.id.action_request_can)
            else -> navigation.navigate(R.id.action_request_pin)
        }
    }

    private fun runWithCardSimulator() {
        navigation.navigate(R.id.action_request_pin)
        navigation.navigate(R.id.password_entered)
        workflowController.setPin(null)
        if (workflow == WorkflowActivity.Workflow.AUTHENTICATE) {
            workflowController.accept()
        }
    }

    fun continueWorkflow() {
        this@WorkflowViewModel.errorMessage.value = null
        navigation.navigate(R.id.action_continue_reading)
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
                workflowEvent.finished(Activity.RESULT_OK, result)
            }

            WorkflowActivity.Workflow.CHANGE_PIN, WorkflowActivity.Workflow.CHANGE_TRANSPORT_PIN -> {
                val result =
                    Bundle().apply {
                        putParcelable(
                            RESULT_CHANGE_PIN,
                            changePinResult,
                        )
                    }
                workflowEvent.finished(Activity.RESULT_OK, result)
            }
        }
    }

    fun showCertificate() {
        navigation.navigate(R.id.action_show_certificate)
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
            navigation.navigate(R.id.action_authentication_aborted)
        }
    }

    fun presetPin(pin: CharArray?) {
        this.pin = pin
    }

    fun presetCan(can: CharArray?) {
        this.can = can
    }

    private fun showErrorMessageIfError(error: String?): Boolean {
        val err = error ?: ""
        if (error != null) {
            errorMessage.value = err
            navigation.navigate(R.id.action_error_occured)
            return true
        }
        return false
    }
}
