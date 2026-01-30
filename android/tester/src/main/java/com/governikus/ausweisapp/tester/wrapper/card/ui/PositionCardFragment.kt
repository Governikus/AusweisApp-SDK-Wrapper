/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui

import android.view.LayoutInflater
import androidx.core.view.WindowInsetsCompat
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentPositionCardBinding

internal class PositionCardFragment : BaseFragment<FragmentPositionCardBinding>() {
    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentPositionCardBinding = FragmentPositionCardBinding.inflate(inflater)

    override fun onApplyInsets(insets: WindowInsetsCompat) {
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        viewBinding?.root?.setPadding(0, 0, 0, systemBarInsets.bottom)
    }
}
