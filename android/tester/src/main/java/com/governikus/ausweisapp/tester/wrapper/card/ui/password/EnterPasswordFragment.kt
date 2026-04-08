/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.password

import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModelFactory
import com.governikus.ausweisapp.tester.wrapper.common.BaseFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.FragmentEnterPasswordBinding

internal class EnterPasswordFragment : BaseFragment<FragmentEnterPasswordBinding>() {
    enum class PasswordType(
        val type: String,
    ) {
        PIN("PIN"),
        TRANSPORT_PIN("TRANSPORT_PIN"),
        CAN("CAN"),
        PUK("PUK"),
        ;

        companion object {
            fun fromString(type: String?) = values().firstOrNull { it.type == type } ?: PIN
        }
    }

    private val viewModel: EnterPasswordViewModel by viewModels(
        factoryProducer = {
            WorkflowFragmentViewModelFactory(
                requireActivity(),
            )
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.passwordType.value =
            PasswordType.fromString(arguments?.getString("passwordType"))
    }

    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentEnterPasswordBinding = FragmentEnterPasswordBinding.inflate(inflater)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val viewBinding = viewBinding ?: return

        viewModel.infoHeader.observe(viewLifecycleOwner) { viewBinding.tvInfoHeader.setText(it) }

        viewModel.infoText.observe(viewLifecycleOwner) { viewBinding.tvInfoText.setText(it) }
        viewModel.infoText.observe(viewLifecycleOwner) { viewBinding.tvInfoText.setVisibility(if (it != null) View.VISIBLE else View.GONE) }

        viewModel.passwordHint.observe(viewLifecycleOwner) { viewBinding.tilPassword.setHint(it) }
        viewModel.showPasswordErrorMessage.observe(viewLifecycleOwner) { viewBinding.tilPassword.setErrorEnabled(it) }
        viewModel.passwordErrorMessage.observe(viewLifecycleOwner) { viewBinding.tilPassword.setError(it) }

        viewModel.passwordInputLength.observe(viewLifecycleOwner) { viewBinding.etPassword.setFilters(arrayOf(LengthFilter(it))) }
        viewBinding.etPassword.setOnClickListener { viewModel.onAccept() }
        viewBinding.etPassword.doOnTextChanged { s, _, _, _ ->
            val password =
                if (s != null) {
                    CharArray(s.length) { index -> s[index] }
                } else {
                    CharArray(0)
                }
            viewModel.password.postValue(password)
            password.fill('\u0000')
        }
        viewBinding.etPassword.setOnEditorActionListener { _, actionId, _ ->
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

        viewModel.pinRetryCounterMessage.observe(viewLifecycleOwner) { viewBinding.tvRetryCounter.setText(it) }
        viewModel.showRetryCounter.observe(
            viewLifecycleOwner,
        ) { viewBinding.tvRetryCounter.setVisibility(if (it) View.VISIBLE else View.GONE) }

        viewModel.isPasswordValid.observe(viewLifecycleOwner) { viewBinding.btnAccept.setEnabled(it) }
        viewBinding.btnAccept.setOnClickListener { viewModel.onAccept() }
        viewBinding.btnAcceptPinpad.setEnabled(viewModel.workflowViewModel.hasPinPadReader.value ?: false)

        viewModel.workflowViewModel.hasPinPadReader.observe(viewLifecycleOwner) { viewBinding.btnAcceptPinpad.setEnabled(it) }
        viewBinding.btnAcceptPinpad.setOnClickListener { viewModel.onAcceptEmptyPassword() }
    }
}
