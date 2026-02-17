package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * UserProfileActivity handles the user's profile and settings menu.
 * This screen is independent and matches the provided design exactly.
 */
class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        setupNavigation()
        setupMenuClickListeners()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupMenuClickListeners() {
        // Example click listeners for menu items
        findViewById<LinearLayout>(R.id.item_privacy)?.setOnClickListener {
            // Handle Privacy Policy click
        }

        findViewById<LinearLayout>(R.id.item_terms)?.setOnClickListener {
            // Handle Terms & Conditions click
        }

        findViewById<LinearLayout>(R.id.item_help)?.setOnClickListener {
            // Handle Help & Feedback click
        }

        findViewById<LinearLayout>(R.id.item_password)?.setOnClickListener {
            // Handle Password Manager click
        }
    }
}