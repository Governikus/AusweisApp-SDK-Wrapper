/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModelFactory
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentErrorBinding

internal class ErrorFragment : BaseFragment<FragmentErrorBinding>() {
    private val viewModel: ErrorFragmentViewModel by viewModels(
        factoryProducer = {
            WorkflowFragmentViewModelFactory(
                requireActivity(),
            )
        },
    )

    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentErrorBinding = FragmentErrorBinding.inflate(inflater)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val viewBinding = viewBinding ?: return

        viewModel.workflowViewModel.errorMessage.observe(viewLifecycleOwner) { viewBinding.tvErrorMessage.setText(it) }

        viewBinding.btnAcceptError.setOnClickListener { viewModel.onAccept() }
    }
}
