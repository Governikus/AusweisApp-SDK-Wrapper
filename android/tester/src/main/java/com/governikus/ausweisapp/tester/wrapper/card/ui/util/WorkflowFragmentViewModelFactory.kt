/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.util

import android.app.Application
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowViewModel
import java.lang.reflect.Constructor

internal class WorkflowFragmentViewModelFactory(
    private val activity: FragmentActivity,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (WorkflowFragmentViewModel::class.java.isAssignableFrom(modelClass)) {
            val constructor =
                modelClass.findMatchingConstructor(WorkflowViewModelSignature)
                    ?: throw IllegalArgumentException("Constructor not found")
            val activityViewModel: WorkflowViewModel by activity.viewModels()
            constructor.newInstance(activityViewModel, activity.application)
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }

    companion object {
        private val WorkflowViewModelSignature: Array<Class<*>> =
            arrayOf(WorkflowViewModel::class.java, Application::class.java)

        private fun <T> Class<T>.findMatchingConstructor(signature: Array<Class<*>>): Constructor<T>? {
            val constructor =
                constructors.firstOrNull {
                    signature.contentEquals(it.parameterTypes)
                }
            @Suppress("UNCHECKED_CAST")
            return constructor as? Constructor<T>
        }
    }
}
