/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat.getParcelable
import com.governikus.ausweisapp.sdkwrapper.card.core.AuthResult
import com.governikus.ausweisapp.sdkwrapper.card.core.ChangePinResult
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.Authentication.SimulatorMode
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowActivity
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowActivity.Companion.PARAM_CARD_SIMULATOR
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowActivity.Companion.PARAM_DEVELOPER_MODE
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowActivity.Companion.PARAM_TC_TOKEN_URL
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowActivity.Workflow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

object AusweisApp2WrapperConnection : CoroutineScope by MainScope() {
    class Authentication : ActivityResultContract<Authentication.Options, AuthResult?>() {
        companion object {
            internal const val RESULT_AUTH = "resultAuth"
        }

        enum class SimulatorMode(
            val mode: String,
        ) {
            DISABLED("disabled"),
            DEFAULT_DATA("defaultData"),
            DIFFERENT_FIRST_NAME("differentFirstName"),
            DIFFERENT_PSEUDONYM("differentPseudonym"),
            ;

            companion object {
                fun fromString(mode: String?): SimulatorMode? = values().firstOrNull { it.mode == mode }
            }
        }

        data class Options(
            val tcTokenUrl: Uri,
            val developerMode: Boolean = false,
            val cardSimulatorMode: SimulatorMode = SimulatorMode.DISABLED,
        )

        override fun createIntent(
            context: Context,
            input: Options,
        ): Intent =
            Intent(
                context,
                WorkflowActivity::class.java,
            ).apply {
                putExtra(WorkflowActivity.PARAM_REQUESTED_WORKFLOW, Workflow.AUTHENTICATE)
                putExtra(PARAM_TC_TOKEN_URL, input.tcTokenUrl)
                putExtra(PARAM_DEVELOPER_MODE, input.developerMode)
                putExtra(PARAM_CARD_SIMULATOR, input.cardSimulatorMode.mode)
            }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?,
        ): AuthResult? = intent?.extras?.let { bundle -> return getParcelable(bundle, RESULT_AUTH, AuthResult::class.java) }
    }

    class ChangePin : ActivityResultContract<ChangePin.Options, ChangePinResult?>() {
        companion object {
            internal const val RESULT_CHANGE_PIN = "resultChangePin"
        }

        data class Options(
            val changeTransportPin: Boolean = false,
        )

        override fun createIntent(
            context: Context,
            input: Options,
        ): Intent =
            Intent(
                context,
                WorkflowActivity::class.java,
            ).apply {
                putExtra(
                    WorkflowActivity.PARAM_REQUESTED_WORKFLOW,
                    if (input.changeTransportPin) Workflow.CHANGE_TRANSPORT_PIN else Workflow.CHANGE_PIN,
                )
            }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?,
        ): ChangePinResult? = intent?.extras?.let { bundle -> return getParcelable(bundle, RESULT_CHANGE_PIN, ChangePinResult::class.java) }
    }

    fun ActivityResultLauncher<Authentication.Options>.authenticate(
        tcTokenUrl: Uri,
        developerMode: Boolean = false,
        cardSimulatorMode: SimulatorMode = SimulatorMode.DISABLED,
    ) {
        launch(Authentication.Options(tcTokenUrl, developerMode, cardSimulatorMode))
    }

    fun ActivityResultLauncher<ChangePin.Options>.changePin() {
        launch(ChangePin.Options())
    }

    fun ActivityResultLauncher<ChangePin.Options>.changeTransportPin() {
        launch(ChangePin.Options(true))
    }
}
