/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.governikus.ausweisapp.tester.wrapper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(viewBinding.navHostFragment.id) as NavHostFragment

        viewBinding.toolbar.setupWithNavController(navHostFragment.navController)
        title = navHostFragment.navController.currentDestination?.label

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewBinding.toolbar.let { toolbar ->
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
                val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, systemBarInsets.top, 0, 0)
                insets
            }
        }
    }
}
