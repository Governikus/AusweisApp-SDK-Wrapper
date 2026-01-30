/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.pause

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModelFactory
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentPauseBinding

internal class PauseFragment : BaseFragment<FragmentPauseBinding>() {
    private val viewModel: PauseFragmentViewModel by viewModels(
        factoryProducer = {
            WorkflowFragmentViewModelFactory(
                requireActivity(),
            )
        },
    )

    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentPauseBinding = FragmentPauseBinding.inflate(inflater)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val viewBinding = viewBinding ?: return
        viewModel.workflowViewModel.errorMessage.observe(
            viewLifecycleOwner,
        ) { errorMessage -> viewBinding.tvPauseCause.text = errorMessage }
        viewBinding.btnContinue.setOnClickListener { _ -> viewModel.onContinue() }
    }

    override fun onApplyInsets(insets: WindowInsetsCompat) {
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        viewBinding?.root?.setPadding(0, 0, 0, systemBarInsets.bottom)
    }
}
