package com.example.smarthome

import android.content.Context
import android.media.AudioManager
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
    private var deviceName = "Bluetooth Device"
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speaker_control)

        deviceId = intent.getStringExtra("DEVICE_ID") ?: "speaker_default"
        deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Bluetooth Device"
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        setupUI()
        handleBackNavigation()
    }

    private fun setupUI() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { 
            onBackPressedDispatcher.onBackPressed()
        }

        val tvTitle = findViewById<TextView>(R.id.tv_speaker_title)
        val tvNowPlaying = findViewById<TextView>(R.id.tv_now_playing)
        val sliderVolume = findViewById<Slider>(R.id.slider_volume)

        tvTitle.text = deviceName
        tvNowPlaying.text = "Linked to System Audio"

        // Set slider range based on system max volume
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        
        sliderVolume.valueFrom = 0f
        sliderVolume.valueTo = maxVolume.toFloat()
        sliderVolume.value = currentVolume.toFloat()

        sliderVolume.addOnChangeListener { _, value, _ ->
            // Update real system volume
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                value.toInt(),
                AudioManager.FLAG_SHOW_UI // Show the system volume bar for visual confirmation
            )
        }

        // Inform user that playback is controlled externally
        val cvPlayPause = findViewById<CardView>(R.id.cv_play_pause)
        cvPlayPause.setOnClickListener {
            Toast.makeText(this, "Use Spotify or Music app for playback", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.iv_next).setOnClickListener {
            Toast.makeText(this, "Control playback via Spotify", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.iv_prev).setOnClickListener {
            Toast.makeText(this, "Control playback via Spotify", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}
