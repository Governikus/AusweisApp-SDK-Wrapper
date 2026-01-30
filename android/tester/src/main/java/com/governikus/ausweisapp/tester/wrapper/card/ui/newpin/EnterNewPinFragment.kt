/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.newpin

import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModelFactory
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentEnterNewPinBinding

internal class EnterNewPinFragment : BaseFragment<FragmentEnterNewPinBinding>() {
    private val viewModel: EnterNewPinViewModel by viewModels(
        factoryProducer = {
            WorkflowFragmentViewModelFactory(
                requireActivity(),
            )
        },
    )

    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentEnterNewPinBinding = FragmentEnterNewPinBinding.inflate(inflater)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = viewBinding ?: return

        viewBinding.etNewPin.setFilters(arrayOf(LengthFilter(viewModel.newPinInputLength)))
        viewBinding.etNewPin.doOnTextChanged { s, _, _, _ ->
            viewModel.newPin.postValue(s.toString())
        }

        viewModel.pinErrorMessage.observe(viewLifecycleOwner) { viewBinding.tilConfirmNewPin.setError(it) }

        viewBinding.etConfirmNewPin.setFilters(arrayOf(LengthFilter(viewModel.newPinInputLength)))
        viewBinding.etConfirmNewPin.setOnClickListener { viewModel.onAccept() }
        viewBinding.etConfirmNewPin.doOnTextChanged { s, _, _, _ ->
            viewModel.confirmationPin.postValue(s.toString())
        }
        viewBinding.etConfirmNewPin.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    viewModel.onAccept()
                    true
                }
                else -> false
            }
        }

        viewModel.isNewPinValid.observe(viewLifecycleOwner) { viewBinding.btnStartPinChange.setEnabled(it) }

        viewBinding.btnStartPinChange.setOnClickListener { viewModel.onAccept() }
    }

    override fun onApplyInsets(insets: WindowInsetsCompat) {
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        viewBinding?.root?.setPadding(0, 0, 0, systemBarInsets.bottom)
    }
}
