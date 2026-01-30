/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.newpin

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.governikus.ausweisapp.sdkwrapper.card.core.WorkflowController
import com.governikus.ausweisapp.tester.wrapper.R
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowViewModel
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModel

internal class EnterNewPinViewModel(
    workflowViewModel: WorkflowViewModel,
    application: Application,
) : WorkflowFragmentViewModel(workflowViewModel, application) {
    val newPin = MutableLiveData<String>()
    val confirmationPin = MutableLiveData<String>()

    val newPinInputLength = WorkflowController.PIN_LENGTH

    val isNewPinValid =
        newPin.switchMap { newPin ->
            confirmationPin.map { confirmationPin ->
                newPin?.length == newPinInputLength && newPin == confirmationPin
            }
        }

    val pinErrorMessage =
        newPin.switchMap { newPin ->
            confirmationPin.map { confirmationPin ->
                if (confirmationPin?.length == newPinInputLength && newPin != confirmationPin) {
                    application.getString(R.string.enter_new_pin_confirmation_error)
                } else {
                    null
                }
            }
        }

    fun onAccept() {
        val newPin = newPin.value ?: return
        val newPinValid = isNewPinValid.value ?: return

        if (!newPinValid) {
            return
        }

        workflowViewModel.setNewPin(newPin)
    }
}
