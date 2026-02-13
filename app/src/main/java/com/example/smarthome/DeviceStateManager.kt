package com.example.smarthome

/**
 * Singleton manager to persist device states during the app session.
 * This ensures that when a user navigates back and returns, the state is maintained.
 */
object DeviceStateManager {
    private val deviceStates = mutableMapOf<String, Boolean>()

    fun setDeviceState(deviceId: String, isOn: Boolean) {
        deviceStates[deviceId] = isOn
    }

    fun getDeviceState(deviceId: String): Boolean {
        return deviceStates[deviceId] ?: false
    }
}