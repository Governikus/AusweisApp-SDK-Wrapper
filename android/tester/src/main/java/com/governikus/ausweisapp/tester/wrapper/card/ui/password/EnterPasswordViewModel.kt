/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.password

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowController
import com.governikus.ausweisapp.tester.wrapper.R
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowViewModel
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModel

internal class EnterPasswordViewModel(
    workflowViewModel: WorkflowViewModel,
    application: Application,
) : WorkflowFragmentViewModel(workflowViewModel, application) {
    var passwordType = MutableLiveData(EnterPasswordFragment.PasswordType.PIN)

    val infoHeader =
        passwordType.map { passwordType ->
            when (passwordType) {
                EnterPasswordFragment.PasswordType.CAN -> application.getString(R.string.enter_password_can_info_header)
                EnterPasswordFragment.PasswordType.PUK -> application.getString(R.string.enter_password_puk_info_header)
                EnterPasswordFragment.PasswordType.TRANSPORT_PIN -> application.getString(R.string.enter_password_transport_pin_info_header)
                else -> application.getString(R.string.enter_password_pin_info_header)
            }
        }

    val infoText =
        passwordType.map { passwordType ->
            when (passwordType) {
                EnterPasswordFragment.PasswordType.CAN -> application.getString(R.string.enter_password_can_info_text)
                EnterPasswordFragment.PasswordType.PUK -> application.getString(R.string.enter_password_puk_info_text)
                else -> null
            }
        }

    val password = MutableLiveData<String>()

    val passwordInputLength =
        passwordType.map { passwordType ->
            when (passwordType) {
                EnterPasswordFragment.PasswordType.CAN -> WorkflowController.CAN_LENGTH
                EnterPasswordFragment.PasswordType.PUK -> WorkflowController.PUK_LENGTH
                EnterPasswordFragment.PasswordType.TRANSPORT_PIN -> WorkflowController.TRANSPORT_PIN_LENGTH
                else -> WorkflowController.PIN_LENGTH
            }
        }

    val passwordHint =
        passwordType.map { passwordType ->
            when (passwordType) {
                EnterPasswordFragment.PasswordType.CAN -> application.getString(R.string.enter_password_can_field_hint)
                EnterPasswordFragment.PasswordType.PUK -> application.getString(R.string.enter_password_puk_field_hint)
                EnterPasswordFragment.PasswordType.TRANSPORT_PIN -> application.getString(R.string.enter_password_transport_pin_field_hint)
                else -> application.getString(R.string.enter_password_pin_field_hint)
            }
        }

    val isPasswordValid =
        passwordInputLength.switchMap { passwordInputLength ->
            password.map { password ->
                !password.isNullOrBlank() && password.length == passwordInputLength
            }
        }

    val pinRetryCounterMessage =
        passwordType.switchMap { passwordType ->
            workflowViewModel.currentCard.map { card ->
                if (!(
                        passwordType == EnterPasswordFragment.PasswordType.PIN ||
                            passwordType == EnterPasswordFragment.PasswordType.TRANSPORT_PIN
                    )
                ) {
                    return@map null
                }

                val retryCounter = card?.pinRetryCounter ?: return@map null
                application.resources.getQuantityString(
                    R.plurals.enter_password_pin_retry_counter_message,
                    retryCounter,
                    retryCounter,
                )
            }
        }
    val showRetryCounter =
        pinRetryCounterMessage.map { pinRetryCounterMessage ->
            pinRetryCounterMessage != null
        }

    val passwordErrorMessage =
        passwordType.switchMap { passwordType ->
            workflowViewModel.lastCard.switchMap { lastCard ->
                workflowViewModel.currentCard.map { currentCard ->
                    when {
                        // Initial password request, show no error
                        currentCard == null -> null
                        // When we have a last card and have to ask for the PIN again or we have to ask for the PUK/CAN after while the retryCounter did not change, the last input was wrong
                        lastCard != null -> {
                            val retryCounterEqual = (lastCard.pinRetryCounter == currentCard.pinRetryCounter)
                            if (
                                (
                                    passwordType == EnterPasswordFragment.PasswordType.PIN ||
                                        passwordType == EnterPasswordFragment.PasswordType.TRANSPORT_PIN
                                ) &&
                                lastCard.pinRetryCounter!! > 0 ||
                                (passwordType == EnterPasswordFragment.PasswordType.PUK && retryCounterEqual) ||
                                (passwordType == EnterPasswordFragment.PasswordType.CAN && retryCounterEqual)
                            ) {
                                application.getString(R.string.enter_password_wrong_try)
                            } else {
                                null
                            }
                        }
                        // If we have no last card and don't have our initial pin/can it means the user gave a wrong input
                        !workflowViewModel.hasStoredPin && !workflowViewModel.hasStoredCan -> {
                            application.getString(R.string.enter_password_wrong_try)
                        }
                        // If we still have our initial pin/can and no last card, it means the AA2 SDK did not ask for it, so we are in a different state than expected.
                        else -> {
                            null
                        }
                    }
                }
            }
        }
    val showPasswordErrorMessage =
        passwordErrorMessage.map { passwordErrorMessage ->
            passwordErrorMessage != null
        }

    fun onAccept() {
        val password = password.value ?: return
        val passwordType = passwordType.value ?: return
        val passwordValid = isPasswordValid.value ?: return

        if (!passwordValid) {
            return
        }

        when (passwordType) {
            EnterPasswordFragment.PasswordType.CAN -> workflowViewModel.setCan(password)
            EnterPasswordFragment.PasswordType.PUK -> workflowViewModel.setPuk(password)
            EnterPasswordFragment.PasswordType.PIN, EnterPasswordFragment.PasswordType.TRANSPORT_PIN -> workflowViewModel.setPin(password)
        }
    }
}
