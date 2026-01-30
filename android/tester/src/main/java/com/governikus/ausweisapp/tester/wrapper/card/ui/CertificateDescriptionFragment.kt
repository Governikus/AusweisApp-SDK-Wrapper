/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentCertificateDescriptionBinding
import java.text.DateFormat
import java.text.DateFormat.MEDIUM
import java.text.SimpleDateFormat

internal class CertificateDescriptionFragment : BaseFragment<FragmentCertificateDescriptionBinding>() {
    private val workflowViewModel: WorkflowViewModel by activityViewModels()

    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentCertificateDescriptionBinding = FragmentCertificateDescriptionBinding.inflate(inflater)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val viewBinding = viewBinding ?: return

        workflowViewModel.certificateDescription.observe(
            viewLifecycleOwner,
            Observer { certificateDescription ->
                viewBinding.tvCertificateIssuerName.setText(certificateDescription.issuerName)

                viewBinding.tvCertificateIssuerUrl.setText(certificateDescription.issuerUrl.toString())

                viewBinding.tvCertificatePurpose.setText(certificateDescription.purpose)

                viewBinding.tvCertificateSubjectName.setText(certificateDescription.subjectName)

                viewBinding.tvCertificateSubjectUrl.setText(certificateDescription.subjectUrl.toString())

                viewBinding.tvCertificateTou.setText(certificateDescription.termsOfUsage)

                val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM)
                viewBinding.tvCertificateIssueDate.setText(dateFormat.format(certificateDescription.validity.effectiveDate))

                viewBinding.tvCertificateExpirationDate.setText(dateFormat.format(certificateDescription.validity.expirationDate))
            },
        )
    }

    override fun onApplyInsets(insets: WindowInsetsCompat) {
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        viewBinding?.root?.setPadding(0, 0, 0, systemBarInsets.bottom)
    }
}
