package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * ProfileActivity displays the user's room profile and categories.
 * This activity is independent and does not modify existing navigation.
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize UI components
        setupClickListeners()
        
        // Handle modern back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupClickListeners() {
        // Back Button Logic
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        // Add additional logic here for cards or search if needed
    }
}