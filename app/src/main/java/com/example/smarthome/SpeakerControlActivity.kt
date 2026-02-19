package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.slider.Slider

class SpeakerControlActivity : AppCompatActivity() {

    private var deviceId = "speaker_default"
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speaker_control)

        deviceId = intent.getStringExtra("DEVICE_ID") ?: "speaker_default"
        
        setupUI()
        handleBackNavigation()
    }

    private fun setupUI() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        val tvNowPlaying = findViewById<TextView>(R.id.tv_now_playing)
        val cvPlayPause = findViewById<CardView>(R.id.cv_play_pause)
        val ivPlayPauseIcon = findViewById<ImageView>(R.id.iv_play_pause_icon)
        val sliderVolume = findViewById<Slider>(R.id.slider_volume)

        // Init State
        isPlaying = DeviceStateManager.getDeviceState(deviceId)
        updatePlayPauseUI(ivPlayPauseIcon)
        sliderVolume.value = DeviceStateManager.getDeviceValue(deviceId, 50).toFloat()

        cvPlayPause.setOnClickListener {
            isPlaying = !isPlaying
            DeviceStateManager.setDeviceState(deviceId, isPlaying)
            updatePlayPauseUI(ivPlayPauseIcon)
            tvNowPlaying.text = if (isPlaying) "Now Playing: Chill Beats" else "Paused"
        }

        sliderVolume.addOnChangeListener { _, value, _ ->
            DeviceStateManager.setDeviceValue(deviceId, value.toInt())
        }

        findViewById<ImageView>(R.id.iv_next).setOnClickListener {
            Toast.makeText(this, "Next Track", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.iv_prev).setOnClickListener {
            Toast.makeText(this, "Previous Track", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePlayPauseUI(icon: ImageView) {
        icon.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause 
            else android.R.drawable.ic_media_play
        )
    }

    private fun handleBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}