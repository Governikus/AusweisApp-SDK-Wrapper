/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui

import android.view.LayoutInflater
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentLoadCertificateBinding

internal class LoadCertificateFragment : BaseFragment<FragmentLoadCertificateBinding>() {
    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentLoadCertificateBinding = FragmentLoadCertificateBinding.inflate(inflater)
}
