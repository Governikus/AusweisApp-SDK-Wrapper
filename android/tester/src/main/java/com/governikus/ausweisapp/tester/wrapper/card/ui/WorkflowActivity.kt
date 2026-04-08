/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.core.content.IntentCompat.getSerializableExtra
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.governikus.ausweisapp.sdkwrapper.SDKWrapper.workflowController
import com.governikus.ausweisapp.sdkwrapper.card.core.NfcForegroundDispatcher
import com.governikus.ausweisapp.tester.wrapper.AusweisApp2WrapperConnection.Authentication.SimulatorMode
import com.governikus.ausweisapp.tester.wrapper.R
import com.governikus.ausweisapp.tester.wrapper.card.ui.password.EnterPasswordFragment
import com.governikus.ausweisapp.tester.wrapper.databinding.ActivityCardWorkflowBinding

internal class WorkflowActivity : AppCompatActivity() {
    internal enum class Workflow {
        AUTHENTICATE,
        CHANGE_PIN,
        CHANGE_TRANSPORT_PIN,
    }

    private lateinit var viewBinding: ActivityCardWorkflowBinding
    private val viewModel: WorkflowViewModel by viewModels()

    private lateinit var nfcDispatcher: NfcForegroundDispatcher

    private val navHostFragment: NavHostFragment
        get() = supportFragmentManager.findFragmentById(viewBinding.navHostFragment.id) as NavHostFragment

    private var lastBackPress: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.SDKTheme)

        viewBinding = ActivityCardWorkflowBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.toolbar) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, systemBarInsets.bottom)
            insets
        }

        val callback =
            object : OnBackPressedCallback(
                true,
            ) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
        onBackPressedDispatcher.addCallback(this, callback)

        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = destination.label
            when (destination.id) {
                R.id.certificate_description -> viewBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
                R.id.error, R.id.authentication_aborted -> viewBinding.toolbar.navigationIcon = null
                else -> viewBinding.toolbar.setNavigationIcon(R.drawable.ic_close_24dp)
            }
        }
        viewBinding.toolbar.setNavigationOnClickListener {
            if (navHostFragment.childFragmentManager.backStackEntryCount > 0) {
                navHostFragment.navController.popBackStack()
                return@setNavigationOnClickListener
            }

            viewModel.cancelWorkflow()
        }

        viewModel.navigation.observe(this) { event ->
            val navEvent = event.getContentIfNotHandled() ?: return@observe
            navController.navigate(navEvent.action, navEvent.data)
        }

        viewModel.workflowEvent.observe(this) { event ->
            val navEvent = event.getContentIfNotHandled() ?: return@observe
            val data =
                navEvent.data?.let { data ->
                    Intent().apply {
                        putExtras(data)
                    }
                }
            setResult(navEvent.action, data)
            finish()
        }

        viewModel.toast.observe(this) { event ->
            val toast = event.getContentIfNotHandled() ?: return@observe
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
        }

        nfcDispatcher = NfcForegroundDispatcher(this, workflowController)

        // Do not reset values when restoring activity
        if (savedInstanceState != null) {
            return
        }

        viewModel.workflow =
            getSerializableExtra(intent, PARAM_REQUESTED_WORKFLOW, Workflow::class.java)
                ?: throw IllegalStateException("No workflow requested")

        when (viewModel.workflow) {
            Workflow.AUTHENTICATE -> {
                viewModel.tcTokenUrl =
                    getParcelableExtra(intent, PARAM_TC_TOKEN_URL, Uri::class.java)
                viewModel.developerMode = intent.getBooleanExtra(PARAM_DEVELOPER_MODE, false)
                viewModel.cardSimulatorMode =
                    SimulatorMode.fromString(intent.getStringExtra(PARAM_CARD_SIMULATOR))
                        ?: SimulatorMode.DISABLED

                navController.navigate(R.id.action_start_authentication)
            }

            Workflow.CHANGE_PIN -> {
                navController.navigate(R.id.action_start_pin_change)
            }

            Workflow.CHANGE_TRANSPORT_PIN -> {
                navController.navigate(
                    R.id.action_start_pin_change,
                    Bundle().apply {
                        putString(
                            "passwordType",
                            EnterPasswordFragment.PasswordType.TRANSPORT_PIN.type,
                        )
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcDispatcher.start()
    }

    override fun onPause() {
        super.onPause()
        nfcDispatcher.stop()
    }

    private fun backPressed() {
        when {
            navHostFragment.childFragmentManager.backStackEntryCount > 0 -> {
                navHostFragment.navController.popBackStack()
            }

            viewModel.workflowStatus == WorkflowViewModel.WorkflowStatus.COMPLETED -> {
                viewModel.acceptError()
            }

            viewModel.workflowStatus == WorkflowViewModel.WorkflowStatus.CANCELLED -> {
                // Do nothing waiting for completion
            }

            lastBackPress + CLOSE_TIME_DELAY > System.currentTimeMillis() -> {
                viewModel.cancelWorkflow()
            }

            else -> {
                lastBackPress = System.currentTimeMillis()
                Toast.makeText(this, R.string.back_press_info, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CLOSE_TIME_DELAY = 2000

        const val PARAM_REQUESTED_WORKFLOW = "requestedWorkflow"
        const val PARAM_TC_TOKEN_URL = "tcTokenUrl"
        const val PARAM_DEVELOPER_MODE = "developerMode"
        const val PARAM_CARD_SIMULATOR = "cardSimulator"
    }
}
