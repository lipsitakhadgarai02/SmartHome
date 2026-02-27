package com.example.smarthome

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SensorMonitoringActivity : AppCompatActivity() {

    private lateinit var temperatureText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_monitoring)

        temperatureText = findViewById(R.id.temperatureText)

        fetchWeather()
    }

    private fun fetchWeather() {
        lifecycleScope.launch {

            val response = RetrofitInstance.api.getWeather(
                "Balasore",
                BuildConfig.WEATHER_API_KEY
            )

            if (response.isSuccessful) {
                val temp = response.body()?.main?.temp
                temperatureText.text = "$temp °C"
            } else {
                temperatureText.text = "Error"
            }
        }
    }
}