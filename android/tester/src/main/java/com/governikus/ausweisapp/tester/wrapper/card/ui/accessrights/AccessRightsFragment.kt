/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.accessrights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModelFactory
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentAccessRightsBinding

internal class AccessRightsFragment : BaseFragment<FragmentAccessRightsBinding>() {
    private val viewModel: AccessRightsFragmentViewModel by viewModels(
        factoryProducer = {
            WorkflowFragmentViewModelFactory(
                requireActivity(),
            )
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.requiredRightsStatus.observe(this) { list ->
            viewModel.requiredRightsAdapter.submitList(list)
        }

        viewModel.optionalRightsStatus.observe(this) { list ->
            viewModel.optionalRightsAdapter.submitList(list)
        }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentAccessRightsBinding = FragmentAccessRightsBinding.inflate(inflater)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = viewBinding ?: return

        viewModel.certificateSubjectName.observe(viewLifecycleOwner) { viewBinding.accessRightsProviderName.setText(it) }

        viewModel.certificatePurpose.observe(viewLifecycleOwner) { viewBinding.accessRightsProviderPurpose.setText(it) }

        viewBinding.btnShowCertificate.setOnClickListener { viewModel.showCertificate() }

        viewModel.hasCertificateDescription.observe(viewLifecycleOwner) {
            viewBinding.groupCertificateDescription.setVisibility(if (it) View.VISIBLE else View.GONE)
        }

        viewModel.hasCertificateDescription.observe(viewLifecycleOwner) {
            viewBinding.accessRightsMissing.setVisibility(if (it) View.GONE else View.VISIBLE)
        }

        viewBinding.btnAcceptAccessRights.setOnClickListener { viewModel.accept() }

        viewModel.hasRequiredRights.observe(viewLifecycleOwner) {
            viewBinding.groupRequiredAccessRights.setVisibility(if (it) View.VISIBLE else View.GONE)
        }

        viewModel.hasOptionalRights.observe(viewLifecycleOwner) {
            viewBinding.groupOptionalAccessRights.setVisibility(if (it) View.VISIBLE else View.GONE)
        }

        viewBinding.requiredAccessRightsContainer.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL,
            ),
        )
        viewBinding.requiredAccessRightsContainer.setHasFixedSize(false)
        viewBinding.requiredAccessRightsContainer.isNestedScrollingEnabled = false
        viewBinding.requiredAccessRightsContainer.adapter = viewModel.requiredRightsAdapter

        viewBinding.optionalAccessRightsContainer.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL,
            ),
        )
        viewBinding.optionalAccessRightsContainer.setHasFixedSize(false)
        viewBinding.optionalAccessRightsContainer.isNestedScrollingEnabled = false
        viewBinding.optionalAccessRightsContainer.adapter = viewModel.optionalRightsAdapter
    }

    override fun onApplyInsets(insets: WindowInsetsCompat) {
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        viewBinding?.scrollViewContent?.setPadding(0, 0, 0, systemBarInsets.bottom)
    }
}
