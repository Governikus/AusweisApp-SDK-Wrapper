/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.Authentication.SimulatorMode
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.authenticate
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentAuthenticationWorkflowBinding

class AuthenticationWorkflowFragment : Fragment(R.layout.fragment_authentication_workflow) {
    private var viewBinding: FragmentAuthenticationWorkflowBinding? = null

    private val authLauncher =
        registerForActivityResult(AusweisApp2WrapperConnection.Authentication()) { result ->
            if (result == null) {
                Toast.makeText(context, R.string.workflow_aborted, Toast.LENGTH_SHORT).show()
            } else {
                viewBinding?.authenticationResult?.authResult = result
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding =
            FragmentAuthenticationWorkflowBinding.bind(view).apply {
                btnAuthenticationStart.setOnClickListener {
                    authLauncher.authenticate(
                        getString(R.string.test_eid_tc_token_url).toUri(),
                    )
                }
                btnAuthenticationStartRequiredRights.setOnClickListener {
                    authLauncher.authenticate(
                        getString(R.string.test_eid_tc_token_url_required_rights).toUri(),
                    )
                }
                btnAuthenticationStartCanAllowed.setOnClickListener {
                    authLauncher.authenticate(
                        getString(R.string.test_eid_can_tc_token_url).toUri(),
                    )
                }
                btnAuthenticationStartDeveloperMode.setOnClickListener {
                    authLauncher.authenticate(
                        getString(R.string.test_eid_developerMode_tc_token_url).toUri(),
                        developerMode = true,
                    )
                }
                btnAuthenticationStartWithCardSimulator.setOnClickListener {
                    val simulatorMode =
                        when {
                            optionSimulatorDefaultData.isChecked -> SimulatorMode.DEFAULT_DATA
                            optionSimulatorDifferentName.isChecked -> SimulatorMode.DIFFERENT_FIRST_NAME
                            optionSimulatorDifferentPseudonym.isChecked -> SimulatorMode.DIFFERENT_PSEUDONYM
                            else -> SimulatorMode.DEFAULT_DATA
                        }

                    authLauncher.authenticate(
                        getString(R.string.test_eid_cardSimulator_tc_token_url).toUri(),
                        cardSimulatorMode = simulatorMode,
                    )
                }

                viewBinding?.root?.let { root ->
                    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                        viewBinding?.scrollViewContent?.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, systemBarInsets.bottom)
                        insets
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}
