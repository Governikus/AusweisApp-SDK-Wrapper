/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.changePin
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.changeTransportPin
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentChangePinWorkflowBinding

class ChangePinWorkflowFragment : Fragment(R.layout.fragment_change_pin_workflow) {
    private var viewBinding: FragmentChangePinWorkflowBinding? = null

    private val changePinLauncher =
        registerForActivityResult(AusweisApp2WrapperConnection.ChangePin()) { result ->
            if (result == null) {
                Toast.makeText(context, R.string.workflow_aborted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, getString(R.string.change_pin_result, result.success, result.reason), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding =
            FragmentChangePinWorkflowBinding.bind(view).apply {
                btnChangePin.setOnClickListener {
                    changePinLauncher.changePin()
                }
                btnTransportChangePin.setOnClickListener {
                    changePinLauncher.changeTransportPin()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}
