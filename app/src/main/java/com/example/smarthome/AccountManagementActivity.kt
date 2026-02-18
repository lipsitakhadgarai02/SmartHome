package com.example.smarthome

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * AccountManagementActivity serves as the dedicated Notifications screen.
 */
class AccountManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_management)

        setupNavigation()
        setupNotificationActions()

        // Modern back press handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupNavigation() {
        // Back Button Logic (btn_close_account is the back arrow in this version)
        findViewById<ImageView>(R.id.btn_close_account)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupNotificationActions() {
        val clearAllButton = findViewById<Button>(R.id.btn_clear_notifications)
        val notificationList = findViewById<LinearLayout>(R.id.ll_notification_list)

        clearAllButton?.setOnClickListener {
            // Hide the notifications and show feedback
            notificationList?.visibility = View.GONE
            Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show()
            
            // Optionally, we could show a "No notifications" placeholder here
        }
    }
}
