package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Onboarding2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_2)

        findViewById<View>(R.id.cl_move_forward).setOnClickListener {
            startActivity(Intent(this, Onboarding3Activity::class.java))
        }

        findViewById<View>(R.id.tv_back).setOnClickListener {
            finish()
        }
    }
}