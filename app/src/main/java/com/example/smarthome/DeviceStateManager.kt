package com.example.smarthome

/**
 * Singleton manager to persist device states during the app session.
 * This ensures that when a user navigates back and returns, the state is maintained.
 */
object DeviceStateManager {
    private val deviceStates = mutableMapOf<String, Boolean>()
    private val deviceValues = mutableMapOf<String, Int>()
    private val pairedDevices = mutableSetOf<String>()
    private val roomNames = mutableListOf<String>()
    
    // Notification State
    var notificationCount: Int = 3
        private set
    var areNotificationsCleared: Boolean = false
        private set

    fun setDeviceState(deviceId: String, isOn: Boolean) {
        deviceStates[deviceId] = isOn
    }

    fun getDeviceState(deviceId: String): Boolean {
        return deviceStates[deviceId] ?: false
    }

    fun setDeviceValue(deviceId: String, value: Int) {
        deviceValues[deviceId] = value
    }

    fun getDeviceValue(deviceId: String, defaultValue: Int = 0): Int {
        return deviceValues[deviceId] ?: defaultValue
    }

    fun pairDevice(deviceId: String) {
        pairedDevices.add(deviceId)
    }

    fun isDevicePaired(deviceId: String): Boolean {
        return pairedDevices.contains(deviceId)
    }

    fun getPairedDevices(): Set<String> {
        return pairedDevices
    }

    fun addRoom(name: String) {
        roomNames.add(name)
    }

    fun getRooms(): List<String> {
        return roomNames
    }
    
    fun clearNotifications() {
        notificationCount = 0
        areNotificationsCleared = true
    }
}