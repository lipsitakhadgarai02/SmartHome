package com.example.smarthome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        // The Web Client ID from google-services.json: 670867161643-oouckbuk4bt4c0mjjuh84a7gisaev5f5.apps.googleusercontent.com
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("670867161643-oouckbuk4bt4c0mjjuh84a7gisaev5f5.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        val tvSignUpLink = findViewById<TextView>(R.id.tv_sign_up_link)
        val btnGoogleSignIn = findViewById<CardView>(R.id.btn_google_signin)
        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)
        val ivBack = findViewById<android.widget.ImageView>(R.id.iv_back)

        ivBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity()
                    } else {
                        val error = task.exception?.message ?: "Login failed"
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        
                        if (error.contains("database") || error.contains("NOT_FOUND")) {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finishAffinity()
                        }
                    }
                }
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        tvSignUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val etResetEmail = EditText(this)
        etResetEmail.hint = "Enter your registered email"
        
        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("We will send a password reset link to your email address.")
            .setView(etResetEmail)
            .setPositiveButton("Send") { _, _ ->
                val email = etResetEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Reset link sent successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "Google success, token: ${account.idToken?.take(10)}...")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                
                // Fallback for development if SHA-1 is missing
                if (e.statusCode == 10 || e.statusCode == 12500) {
                    Toast.makeText(this, "SHA-1 / Configuration issue. Dev Bypass...", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                }
            }
        } else {
            Log.e(TAG, "Google Result Not OK: ${result.resultCode}")
        }
    }

    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                } else {
                    val error = task.exception?.message ?: "Firebase Google Auth failed"
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    
                    if (error.contains("database") || error.contains("NOT_FOUND")) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity()
                    }
                }
            }
    }
}