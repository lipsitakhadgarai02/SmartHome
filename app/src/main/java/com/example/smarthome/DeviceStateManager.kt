package com.example.smarthome

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.random.Random

/**
 * Singleton manager to persist device states using Firebase Firestore.
 * This ensures data is saved across sessions and synced in real-time.
 */
object DeviceStateManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val deviceStates = mutableMapOf<String, Boolean>()
    private val deviceValues = mutableMapOf<String, Int>()
    private val pairedDevices = mutableSetOf<String>()
    private val roomDevices = mutableMapOf<String, List<String>>()
    private val deviceUsage = mutableMapOf<String, Double>()
    
    var notificationCount: Int = 3
    var areNotificationsCleared: Boolean = false

    // Unique: Callback for Activities to listen for real-time cloud changes
    private var onDataChangedListener: (() -> Unit)? = null

    fun setOnDataChangedListener(listener: () -> Unit) {
        onDataChangedListener = listener
    }

    private fun getUserDoc() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid)
    }

    init {
        startListening()
    }

    private fun startListening() {
        getUserDoc()?.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: return@addSnapshotListener
                
                (data["deviceStates"] as? Map<String, Boolean>)?.let { 
                    deviceStates.clear()
                    deviceStates.putAll(it) 
                }
                
                (data["deviceValues"] as? Map<String, Long>)?.let { 
                    deviceValues.clear()
                    it.forEach { (k, v) -> deviceValues[k] = v.toInt() }
                }
                
                (data["pairedDevices"] as? List<String>)?.let { 
                    pairedDevices.clear()
                    pairedDevices.addAll(it)
                }
                
                (data["roomDevices"] as? Map<String, List<String>>)?.let {
                    roomDevices.clear()
                    roomDevices.putAll(it)
                }

                (data["deviceUsage"] as? Map<String, Double>)?.let {
                    deviceUsage.clear()
                    deviceUsage.putAll(it)
                }
                
                notificationCount = (data["notificationCount"] as? Long)?.toInt() ?: 0
                areNotificationsCleared = data["areNotificationsCleared"] as? Boolean ?: false

                // Notify the active screen that data came from the cloud!
                onDataChangedListener?.invoke()
            }
        }
    }

    private fun updateFirestore() {
        val data = mapOf(
            "deviceStates" to deviceStates,
            "deviceValues" to deviceValues,
            "pairedDevices" to pairedDevices.toList(),
            "roomDevices" to roomDevices,
            "deviceUsage" to deviceUsage,
            "notificationCount" to notificationCount,
            "areNotificationsCleared" to areNotificationsCleared
        )
        getUserDoc()?.set(data, SetOptions.merge())
    }

    fun setDeviceState(deviceId: String, isOn: Boolean) {
        deviceStates[deviceId] = isOn
        updateFirestore()
    }

    fun getDeviceState(deviceId: String): Boolean = deviceStates[deviceId] ?: false

    fun setDeviceValue(deviceId: String, value: Int) {
        deviceValues[deviceId] = value
        updateFirestore()
    }

    fun getDeviceValue(deviceId: String, defaultValue: Int = 0): Int = deviceValues[deviceId] ?: defaultValue

    // Calculate total live power load for the dashboard
    fun getTotalLiveLoad(): String {
        var total = 0.0
        if (getDeviceState("ac_living_room")) total += 1.5
        if (getDeviceState("fan_unit_1")) total += 0.2
        if (getDeviceState("lamp_bedroom")) total += 0.05
        return String.format("%.2f kW", total)
    }

    fun getSimulatedRPM(deviceId: String): Int {
        if (!getDeviceState(deviceId)) return 0
        val speed = getDeviceValue(deviceId, 1)
        return (speed * 300) + Random.nextInt(-20, 20)
    }

    fun getEnergyUsage(deviceId: String): String {
        val current = deviceUsage[deviceId] ?: (Random.nextDouble(0.5, 2.5))
        if (getDeviceState(deviceId)) {
            val updated = current + 0.001
            deviceUsage[deviceId] = updated
            return String.format("%.3f kWh", updated)
        }
        return String.format("%.3f kWh", current)
    }

    fun pairDevice(deviceId: String) {
        pairedDevices.add(deviceId)
        updateFirestore()
    }

    fun unpairDevice(deviceId: String) {
        pairedDevices.remove(deviceId)
        updateFirestore()
    }

    fun isDevicePaired(deviceId: String): Boolean = pairedDevices.contains(deviceId)
    fun getRooms(): List<String> = roomDevices.keys.toList()
    fun getDevicesInRoom(roomName: String): List<String> = roomDevices[roomName] ?: emptyList()
    
    fun addRoom(name: String, devices: List<String>) {
        roomDevices[name] = devices
        updateFirestore()
    }

    fun clearNotifications() {
        notificationCount = 0
        areNotificationsCleared = true
        updateFirestore()
    }
}
