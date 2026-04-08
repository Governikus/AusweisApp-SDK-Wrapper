/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentReadingProgressBinding

internal class ReadingProgressFragment : BaseFragment<FragmentReadingProgressBinding>() {
    private val workflowViewModel: WorkflowViewModel by activityViewModels()

    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentReadingProgressBinding = FragmentReadingProgressBinding.inflate(inflater)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val viewBinding = viewBinding ?: return

        workflowViewModel.workflowProgress.observe(
            viewLifecycleOwner,
            Observer { workflowProgress ->
                val progress: Int = workflowProgress.progress ?: 0
                viewBinding.readingProgressLoadingBar.setProgress(progress, true)
            },
        )
    }
}
