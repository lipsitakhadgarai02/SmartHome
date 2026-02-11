package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * AccountManagementActivity allows users to manage their profile, home settings, and notifications.
 * This screen is designed as an independent module.
 */
class AccountManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_management)

        setupClickListeners()

        // Modern back press handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupClickListeners() {
        // Close Button Logic
        findViewById<ImageView>(R.id.btn_close_account)?.setOnClickListener {
            finish()
        }

        // Additional menu item logic can be added here
    }
}