/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.util

import android.app.Application
import androidx.lifecycle.ViewModel
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowViewModel

internal open class WorkflowFragmentViewModel(
    val workflowViewModel: WorkflowViewModel,
    val application: Application,
) : ViewModel()
