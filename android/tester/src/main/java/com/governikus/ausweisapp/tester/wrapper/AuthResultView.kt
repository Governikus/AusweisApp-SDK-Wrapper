/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.governikus.ausweisapp.sdkwrapper.card.core.AuthResult
import com.governikus.ausweisapp.tester.wrapper.databinding.ViewAuthResultBinding

class AuthResultView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
    ) : FrameLayout(context, attrs, defStyle) {
        val viewBinding = ViewAuthResultBinding.inflate(LayoutInflater.from(context), this, true)

        var authResult: AuthResult? = null
            set(value) {
                field = value
                updateView()
            }

        private fun updateView() {
            viewBinding.btnAuthenticationResult.isVisible = authResult != null

            if (authResult?.result?.major == "http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok") {
                viewBinding.btnAuthenticationResult.text =
                    context.getString(R.string.auth_result_success_message_title)
                viewBinding.btnAuthenticationResult.strokeColor =
                    ColorStateList.valueOf(Color.GREEN)
            } else {
                viewBinding.btnAuthenticationResult.text =
                    context.getString(R.string.auth_result_error_message_title, authResult?.result?.message)
                viewBinding.btnAuthenticationResult.strokeColor =
                    ColorStateList.valueOf(Color.RED)
            }

            viewBinding.btnAuthenticationResult.setOnClickListener {
                val url = authResult?.url ?: return@setOnClickListener
                context.startActivity(Intent(Intent.ACTION_VIEW, url))
            }
        }
    }
