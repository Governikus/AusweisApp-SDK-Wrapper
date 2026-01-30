/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.governikus.ausweisapp.tester.sdk.TesterActivity
import com.governikus.ausweisapp.tester.sdk.WebsocketActivity
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentWorkflowSelectionBinding

class WorkflowSelectionFragment : Fragment(R.layout.fragment_workflow_selection) {
    private var viewBinding: FragmentWorkflowSelectionBinding? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding =
            FragmentWorkflowSelectionBinding.bind(view).apply {
                val navController = root.findNavController()
                btnStartAuthenticationWorkflow.setOnClickListener {
                    navController.navigate(R.id.action_start_authentication_workflow)
                }
                btnStartChangePinWorkflow.setOnClickListener {
                    navController.navigate(R.id.action_start_pin_change_workflow)
                }
                btnTesterIntegrated.setOnClickListener {
                    TesterActivity.callActivity(view.context)
                }
                btnWebsocketIntegrated.setOnClickListener {
                    WebsocketActivity.callActivity(view.context)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}
