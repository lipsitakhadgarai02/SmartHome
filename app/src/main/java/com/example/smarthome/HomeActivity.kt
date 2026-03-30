package com.example.smarthome

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var speechRecognizer: SpeechRecognizer
    private val handler = Handler(Looper.getMainLooper())
    private val dashboardUpdateRunnable = object : Runnable {
        override fun run() {
            updateDashboard()
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupDrawer()
        setupBottomNavigation()
        setupClickListeners()
        handleBackNavigation()
        setupRealTimeDate()
        fetchRealWeather()
        updateUserInfo()
        applyWindowInsets()
        initVoiceAssistant()
        
        handler.post(dashboardUpdateRunnable)
    }

    override fun onResume() {
        super.onResume()
        syncDeviceStates()
        updateNotificationBadge()
        updateUserInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(dashboardUpdateRunnable)
        speechRecognizer.destroy()
    }

    private fun updateDashboard() {
        val tvPower = findViewById<TextView>(R.id.tv_power_kw)
        val tvAmps = findViewById<TextView>(R.id.tv_current_amps)
        val tvStatus = findViewById<TextView>(R.id.tv_status_indicator)
        val tvTime = findViewById<TextView>(R.id.tv_active_time)

        val power = DeviceStateManager.getTotalLiveLoad()
        val amps = DeviceStateManager.getCurrentInAmps()
        val status = DeviceStateManager.getUsageStatus()
        val minutes = DeviceStateManager.getTotalActiveTimeMinutes()

        tvPower?.text = String.format("%.2f kW", power)
        tvAmps?.text = String.format("%.2f A", amps)
        tvStatus?.text = status
        
        val h = minutes / 60
        val m = minutes % 60
        tvTime?.text = "${h}h ${m}m"

        // Update status color
        when (status) {
            "Good", "Ideal" -> tvStatus?.setBackgroundResource(R.drawable.bg_rounded_teal)
            "Moderate" -> tvStatus?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
            "High" -> tvStatus?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        }
    }

    private fun initVoiceAssistant() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@HomeActivity, "Listening...", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processVoiceCommand(matches[0].lowercase())
                }
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    else -> "Voice command failed. Try again."
                }
                Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        findViewById<FloatingActionButton>(R.id.fab_voice)?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                speechRecognizer.startListening(speechIntent)
            }
        }
    }

    private fun processVoiceCommand(command: String) {
        Toast.makeText(this, "Command: $command", Toast.LENGTH_LONG).show()
        
        when {
            command.contains("ac on") || command.contains("turn on ac") -> {
                DeviceStateManager.setDeviceState("ac_living_room", true)
                Toast.makeText(this, "Turning on AC", Toast.LENGTH_SHORT).show()
            }
            command.contains("ac off") || command.contains("turn off ac") -> {
                DeviceStateManager.setDeviceState("ac_living_room", false)
            }
            command.contains("light on") || command.contains("turn on light") -> {
                DeviceStateManager.setDeviceState("lamp_bedroom", true)
            }
            command.contains("light off") || command.contains("turn off light") -> {
                DeviceStateManager.setDeviceState("lamp_bedroom", false)
            }
            command.contains("fan on") || command.contains("turn on fan") -> {
                DeviceStateManager.setDeviceState("fan_unit_1", true)
            }
            command.contains("fan off") || command.contains("turn off fan") -> {
                DeviceStateManager.setDeviceState("fan_unit_1", false)
            }
            command.contains("tv on") || command.contains("turn on tv") -> {
                DeviceStateManager.setDeviceState("tv_living_room", true)
            }
            command.contains("tv off") || command.contains("turn off tv") -> {
                DeviceStateManager.setDeviceState("tv_living_room", false)
            }
            else -> Toast.makeText(this, "Sorry, I didn't get that command", Toast.LENGTH_SHORT).show()
        }
        syncDeviceStates()
    }

    private fun updateUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val tvUserName = findViewById<TextView>(R.id.tv_user_name)
            val name = currentUser.displayName ?: currentUser.email?.split("@")?.get(0) ?: "User"
            tvUserName?.text = name

            val navigationView = findViewById<NavigationView>(R.id.navigation_view)
            val headerView = navigationView.getHeaderView(0)
            val tvHeaderName = headerView.findViewById<TextView>(R.id.tv_header_name)
            val tvHeaderEmail = headerView.findViewById<TextView>(R.id.tv_header_email)
            
            tvHeaderName?.text = name
            tvHeaderEmail?.text = currentUser.email ?: ""
        }
    }

    private fun updateNotificationBadge() {
        val badge = findViewById<TextView>(R.id.tv_notification_badge)
        val count = DeviceStateManager.notificationCount
        
        if (count > 0 && !DeviceStateManager.areNotificationsCleared) {
            badge?.text = count.toString()
            badge?.visibility = View.VISIBLE
        } else {
            badge?.visibility = View.GONE
        }
    }

    private fun syncDeviceStates() {
        updateDeviceUI("ac_living_room", findViewById(R.id.cv_ac))
        updateDeviceUI("tv_living_room", findViewById(R.id.cv_tv))
        updateDeviceUI("lamp_bedroom", findViewById(R.id.cv_lamp))
        updateDeviceUI("fan_unit_1", findViewById(R.id.cv_fan))
    }

    private fun updateDeviceUI(deviceId: String, card: CardView?) {
        val isPaired = DeviceStateManager.isDevicePaired(deviceId)
        val isOn = DeviceStateManager.getDeviceState(deviceId)
        card?.alpha = if (isPaired) 1.0f else 0.5f
        
        // Visual feedback for ON/OFF state
        if (isOn) {
            card?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_mint))
        } else {
            card?.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        }
    }

    private fun setupRealTimeDate() {
        val tvDate = findViewById<TextView>(R.id.tv_card_date) ?: return
        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
        tvDate.text = sdf.format(Date())
    }

    private fun fetchRealWeather() {
        val tvTemp = findViewById<TextView>(R.id.tv_home_temp)
        val tvHumidity = findViewById<TextView>(R.id.tv_home_humidity)

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getWeather("Bhubaneswar", BuildConfig.WEATHER_API_KEY)
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!
                    tvTemp?.text = "${weather.main.temp.toInt()}°"
                    tvHumidity?.text = "Humidity: ${weather.main.humidity}%"
                }
            } catch (e: Exception) {
                tvTemp?.text = "28°"
                tvHumidity?.text = "Humidity: 45%"
            }
        }
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val ivMenu = findViewById<ImageView>(R.id.iv_menu)

        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_home -> {}
                R.id.drawer_devices -> startActivity(Intent(this, DeviceScanActivity::class.java))
                R.id.drawer_logout -> performLogout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.nav_home)?.setOnClickListener {}
        findViewById<View>(R.id.nav_search)?.setOnClickListener {
            startActivity(Intent(this, RoomsActivity::class.java))
        }
        findViewById<View>(R.id.nav_devices)?.setOnClickListener {
            startActivity(Intent(this, DeviceScanActivity::class.java))
        }
        findViewById<View>(R.id.nav_settings)?.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cv_energy)?.setOnClickListener {
            startActivity(Intent(this, DeviceManagementActivity::class.java).apply {
                putExtra("FLOW_TYPE", "SENSOR_MONITORING")
            })
        }

        findViewById<Button>(R.id.btn_go_devices)?.setOnClickListener {
            startActivity(Intent(this, DeviceScanActivity::class.java))
        }

        findViewById<CardView>(R.id.cv_ac)?.setOnClickListener { 
            handleDeviceNavigation("ac_living_room", "Air Conditioner", AcControlActivity::class.java)
        }
        findViewById<CardView>(R.id.cv_tv)?.setOnClickListener { 
            handleDeviceNavigation("tv_living_room", "Smart TV", TvRemoteActivity::class.java)
        }
        findViewById<CardView>(R.id.cv_lamp)?.setOnClickListener { 
            handleDeviceNavigation("lamp_bedroom", "Lamp", LampControlActivity::class.java)
        }
        findViewById<CardView>(R.id.cv_fan)?.setOnClickListener { 
            handleDeviceNavigation("fan_unit_1", "Smart Fan", FanControlActivity::class.java)
        }

        findViewById<View>(R.id.fl_notifications)?.setOnClickListener {
            startActivity(Intent(this, AccountManagementActivity::class.java))
        }
    }

    private fun handleDeviceNavigation(deviceId: String, name: String, target: Class<*>) {
        if (DeviceStateManager.isDevicePaired(deviceId)) {
            startActivity(Intent(this, target).apply {
                putExtra("DEVICE_ID", deviceId)
            })
        } else {
            showPairingDialog(name, deviceId)
        }
    }

    private fun showPairingDialog(deviceName: String, deviceId: String) {
        AlertDialog.Builder(this)
            .setTitle("Pair Device")
            .setMessage("Would you like to pair with $deviceName?")
            .setPositiveButton("Pair") { _, _ ->
                DeviceStateManager.pairDevice(deviceId)
                Toast.makeText(this, "$deviceName paired successfully!", Toast.LENGTH_SHORT).show()
                syncDeviceStates()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyWindowInsets() {
        val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomNavContainer?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val density = resources.displayMetrics.density
                bottomMargin = (24 * density).toInt() + systemBars.bottom
            }
            insets
        }
    }

    private fun handleBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, SignInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
