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
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class UsageDashboardActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var deviceListContainer: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            DeviceStateManager.simulateUsageIncrement()
            updateDashboardStats()
            handler.postDelayed(this, 1000) // Update every 1 second for real-time feel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage_dashboard)

        deviceListContainer = findViewById(R.id.ll_device_list)
        
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        initVoiceAssistant()
        populateDeviceBreakdown()

        DeviceStateManager.setOnDataChangedListener {
            runOnUiThread { updateDashboardStats() }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateDashboardStats() {
        val totalPower = DeviceStateManager.getTotalLiveLoad()
        val amps = DeviceStateManager.getCurrentInAmps()
        val status = DeviceStateManager.getUsageStatus()

        findViewById<TextView>(R.id.tv_total_power).text = String.format("%.2f kW", totalPower)
        findViewById<TextView>(R.id.tv_amps).text = String.format("Current: %.2f A", amps)
        findViewById<TextView>(R.id.tv_status).text = "Status: $status"

        // Efficiency simulation
        val efficiency = if (totalPower > 0) 90 + Random().nextInt(8) else 100
        findViewById<TextView>(R.id.tv_efficiency).text = "$efficiency%"

        // Update Usage Analytics Graph
        updateGraphBars()

        // Update list values
        for (i in 0 until deviceListContainer.childCount) {
            val itemView = deviceListContainer.getChildAt(i)
            val deviceId = itemView.tag as? String ?: continue
            
            val usageTv = itemView.findViewById<TextView>(R.id.tv_usage_val)
            usageTv.text = DeviceStateManager.getEnergyUsageFormatted(deviceId)

            val timeTv = itemView.findViewById<TextView>(R.id.tv_time_used)
            val totalMs = DeviceStateManager.getActiveTimeMs(deviceId)
            val minutes = totalMs / 60000
            val h = minutes / 60
            val m = minutes % 60
            timeTv.text = "${h}h ${m}m"
        }
    }

    private fun updateGraphBars() {
        val acUsage = DeviceStateManager.getEnergyUsage("ac_living_room")
        val tvUsage = DeviceStateManager.getEnergyUsage("tv_living_room")
        val lampUsage = DeviceStateManager.getEnergyUsage("lamp_bedroom")
        
        val maxUsage = maxOf(acUsage, tvUsage, lampUsage, 0.1)
        
        setBarWeight(findViewById(R.id.view_bar_ac), (acUsage / maxUsage).toFloat())
        setBarWeight(findViewById(R.id.view_bar_tv), (tvUsage / maxUsage).toFloat())
        setBarWeight(findViewById(R.id.view_bar_lamp), (lampUsage / maxUsage).toFloat())
    }

    private fun setBarWeight(view: View?, weight: Float) {
        val params = view?.layoutParams as? LinearLayout.LayoutParams ?: return
        params.weight = weight.coerceIn(0.05f, 1.0f)
        val parent = view.parent as? LinearLayout ?: return

        // Find the spacer/empty view (usually the second child in this layout pattern)
        if (parent.childCount > 1) {
            val emptyParams = parent.getChildAt(1).layoutParams as? LinearLayout.LayoutParams
            if (emptyParams != null) {
                emptyParams.weight = 1.0f - params.weight
            }
        }
        view.requestLayout()
    }

    private fun populateDeviceBreakdown() {
        deviceListContainer.removeAllViews()
        val devices = listOf(
            Triple("ac_living_room", "Air Conditioner", R.drawable.ic_ac),
            Triple("fan_unit_1", "Smart Fan", R.drawable.ic_fan),
            Triple("lamp_bedroom", "Bedroom Lights", R.drawable.ic_bulb),
            Triple("tv_living_room", "Living Room TV", R.drawable.ic_tv)
        )

        val inflater = LayoutInflater.from(this)
        for (device in devices) {
            val itemView = inflater.inflate(R.layout.item_device_usage, deviceListContainer, false)
            itemView.tag = device.first
            itemView.findViewById<ImageView>(R.id.iv_device_icon).setImageResource(device.third)
            itemView.findViewById<TextView>(R.id.tv_device_name).text = device.second
            itemView.findViewById<TextView>(R.id.tv_room_name).text = if (device.first.contains("bedroom")) "Bedroom" else "Living Room"
            
            val usageTv = itemView.findViewById<TextView>(R.id.tv_usage_val)
            usageTv.text = DeviceStateManager.getEnergyUsageFormatted(device.first)

            val timeTv = itemView.findViewById<TextView>(R.id.tv_time_used)
            val totalMs = DeviceStateManager.getActiveTimeMs(device.first)
            val minutes = totalMs / 60000
            val h = minutes / 60
            val m = minutes % 60
            timeTv.text = "${h}h ${m}m"

            deviceListContainer.addView(itemView)
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
                Toast.makeText(this@UsageDashboardActivity, "Listening...", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@UsageDashboardActivity, "Voice error. Try again.", Toast.LENGTH_SHORT).show()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        findViewById<FloatingActionButton>(R.id.fab_voice).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                speechRecognizer.startListening(speechIntent)
            }
        }
    }

    private fun processVoiceCommand(command: String) {
        when {
            command.contains("ac on") -> DeviceStateManager.setDeviceState("ac_living_room", true)
            command.contains("ac off") -> DeviceStateManager.setDeviceState("ac_living_room", false)
            command.contains("light on") -> DeviceStateManager.setDeviceState("lamp_bedroom", true)
            command.contains("light off") -> DeviceStateManager.setDeviceState("lamp_bedroom", false)
            command.contains("fan on") -> DeviceStateManager.setDeviceState("fan_unit_1", true)
            command.contains("fan off") -> DeviceStateManager.setDeviceState("fan_unit_1", false)
            else -> Toast.makeText(this, "Unknown command", Toast.LENGTH_SHORT).show()
        }
        updateDashboardStats()
    }

    override fun onDestroy() {
        super.onDestroy()
        DeviceStateManager.setOnDataChangedListener {}
        speechRecognizer.destroy()
    }
}