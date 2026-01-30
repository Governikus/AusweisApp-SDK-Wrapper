/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.pause

import android.app.Application
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowViewModel
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModel

internal class PauseFragmentViewModel(
    workflowViewModel: WorkflowViewModel,
    application: Application,
) : WorkflowFragmentViewModel(workflowViewModel, application) {
    fun onContinue() {
        workflowViewModel.continueWorkflow()
    }
}
