package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * TvRemoteActivity provides a digital remote interface for Smart TV control.
 * This activity is independent and follows the project's clean architecture.
 */
class TvRemoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_remote)

        setupUI()
        
        // Handle modern back press logic
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupUI() {
        // Back Button Logic
        findViewById<ImageView>(R.id.iv_back_remote)?.setOnClickListener {
            finish()
        }

        // Potential for adding click listeners for D-pad, Volume, etc.
    }
}