package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DeviceManagementActivity handles device status and monitoring.
 */
class DeviceManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_management)

        setupNavigation()
        setupRealTime()
        updateUserInfo()
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
        if (intent.getBooleanExtra("IS_SAVED", false)) {
            Toast.makeText(this, "Automation Saved Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val tvUserName = findViewById<TextView>(R.id.tv_user_name)
            val tvSubName = findViewById<TextView>(R.id.tv_sub_name)
            val name = currentUser.displayName ?: currentUser.email?.split("@")?.get(0) ?: "User"
            tvUserName?.text = name
            tvSubName?.text = name
        }
    }

    private fun setupNavigation() {
        val roomsContainer = findViewById<LinearLayout>(R.id.ll_rooms)
        roomsContainer?.getChildAt(0)?.setOnClickListener {
            startActivity(Intent(this, DeviceDiscoveryActivity::class.java).apply {
                putExtra("CREATE_MODE", "AUTOMATION")
            })
        }
    }

    private fun setupRealTime() {
        val tvTime = findViewById<TextView>(R.id.tv_time)
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        tvTime?.text = sdf.format(Date())
    }
}
