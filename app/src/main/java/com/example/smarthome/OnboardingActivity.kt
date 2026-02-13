package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        findViewById<View>(R.id.fl_get_in).setOnClickListener {
            startActivity(Intent(this, Onboarding2Activity::class.java))
        }
    }
}