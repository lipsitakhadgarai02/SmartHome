package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Onboarding3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_3)

        findViewById<View>(R.id.cl_get_start).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
        }

        findViewById<View>(R.id.tv_back).setOnClickListener {
            finish()
        }
    }
}