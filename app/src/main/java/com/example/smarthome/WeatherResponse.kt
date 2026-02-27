package com.example.smarthome

data class WeatherResponse(
    val main: Main
)

data class Main(
    val temp: Double,
    val humidity: Int
)