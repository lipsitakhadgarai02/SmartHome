package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * ProfileActivity handles the Search categories.
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        setupClickListeners()
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener { finish() }
    }
}