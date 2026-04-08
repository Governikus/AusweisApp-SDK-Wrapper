/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.newpin

import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
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
            val newPin =
                if (s != null) {
                    CharArray(s.length) { index -> s[index] }
                } else {
                    CharArray(0)
                }
            viewModel.newPin.postValue(newPin)
            newPin.fill('\u0000')
        }

        viewModel.pinErrorMessage.observe(viewLifecycleOwner) { viewBinding.tilConfirmNewPin.setError(it) }

        viewBinding.etConfirmNewPin.setFilters(arrayOf(LengthFilter(viewModel.newPinInputLength)))
        viewBinding.etConfirmNewPin.setOnClickListener { viewModel.onAccept() }
        viewBinding.etConfirmNewPin.doOnTextChanged { s, _, _, _ ->
            val confirmationPin =
                if (s != null) {
                    CharArray(s.length) { index -> s[index] }
                } else {
                    CharArray(0)
                }
            viewModel.confirmationPin.postValue(confirmationPin)
            confirmationPin.fill('\u0000')
        }
        viewBinding.etConfirmNewPin.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    viewModel.onAccept()
                    true
                }

                else -> {
                    false
                }
            }
        }

        viewBinding.btnStartPinChangePinpad.setEnabled(viewModel.workflowViewModel.hasPinPadReader.value ?: false)
        viewModel.isNewPinValid.observe(viewLifecycleOwner) { viewBinding.btnStartPinChange.setEnabled(it) }
        viewModel.workflowViewModel.hasPinPadReader.observe(viewLifecycleOwner) { viewBinding.btnStartPinChangePinpad.setEnabled(it) }

        viewBinding.btnStartPinChange.setOnClickListener { viewModel.onAccept() }
        viewBinding.btnStartPinChangePinpad.setOnClickListener { viewModel.onAcceptEmptyPassword() }
    }
}
