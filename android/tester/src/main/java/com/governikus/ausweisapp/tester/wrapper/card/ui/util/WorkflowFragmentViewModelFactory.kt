/*
 * Copyright (c) 2020-2026 Governikus Service GmbH, Germany
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
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(value = WorkflowFragmentViewModel::class.java.isAssignableFrom(modelClass)) { "Unknown ViewModel class" }

        val constructor = modelClass.findMatchingConstructor(signature = WorkflowViewModelSignature)
        requireNotNull(value = constructor) { "Constructor not found" }

        val activityViewModel: WorkflowViewModel by activity.viewModels()
        return constructor.newInstance(activityViewModel, activity.application)
    }

    companion object {
        private val WorkflowViewModelSignature: Array<Class<*>> =
            arrayOf(WorkflowViewModel::class.java, Application::class.java)

        private fun <T> Class<T>.findMatchingConstructor(signature: Array<Class<*>>): Constructor<T>? {
            val constructor =
                constructors.firstOrNull {
                    signature.contentEquals(other = it.parameterTypes)
                }
            @Suppress("UNCHECKED_CAST")
            return constructor as? Constructor<T>
        }
    }
}
