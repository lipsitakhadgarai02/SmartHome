package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * UserProfileActivity handles the user's profile and settings menu.
 * This version allows editing the profile name directly on the screen.
 */
class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        setupNavigation()
        setupMenuClickListeners()
        setupProfileEditing()
    }

    private fun setupNavigation() {
        // Back Button Logic
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupProfileEditing() {
        val etProfileName = findViewById<EditText>(R.id.tv_profile_name)
        
        etProfileName?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                
                val newName = etProfileName.text.toString()
                if (newName.isNotBlank()) {
                    Toast.makeText(this, "Profile name updated to: $newName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
                etProfileName.clearFocus()
                false // Let the system hide the keyboard
            } else {
                false
            }
        }
    }

    private fun setupMenuClickListeners() {
        // Manage Home
        findViewById<LinearLayout>(R.id.item_manage_home)?.setOnClickListener {
            // Navigate to RoomsActivity which handles room management
            startActivity(Intent(this, RoomsActivity::class.java))
        }

        // Notifications Settings
        findViewById<LinearLayout>(R.id.item_notifications_settings)?.setOnClickListener {
            startActivity(Intent(this, AccountManagementActivity::class.java))
        }

        // Privacy Policy
        findViewById<LinearLayout>(R.id.item_privacy)?.setOnClickListener {
            showInfoDialog("Privacy Policy", "This app is an academic project. No user data is collected, stored, or shared. All logic is local to this device session.")
        }

        // Terms & Conditions
        findViewById<LinearLayout>(R.id.item_terms)?.setOnClickListener {
            showInfoDialog("Terms & Conditions", "By using this demo app, you acknowledge that this is a simulated environment for educational purposes only. Do not enter real sensitive information.")
        }

        // Help & Feedback
        findViewById<LinearLayout>(R.id.item_help)?.setOnClickListener {
            showInfoDialog("Help & Feedback", "Need help? Since this is a demo, please refer to the project documentation or contact the developer directly.")
        }

        // Password Manager
        findViewById<LinearLayout>(R.id.item_password)?.setOnClickListener {
            showPasswordManagerDialog()
        }
    }

    /**
     * Shows a simple information dialog for Privacy, Terms, or Help.
     */
    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    /**
     * Simulates a password change flow with validation.
     */
    private fun showPasswordManagerDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (24 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val etNewPassword = EditText(context).apply {
            hint = "New Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val etConfirmPassword = EditText(context).apply {
            hint = "Confirm New Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etNewPassword)
        layout.addView(etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val newPass = etNewPassword.text.toString()
                val confirmPass = etConfirmPassword.text.toString()

                when {
                    newPass.isEmpty() || confirmPass.isEmpty() -> {
                        Toast.makeText(context, "Password fields cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                    newPass != confirmPass -> {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(context, "Password updated successfully (Demo)", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
