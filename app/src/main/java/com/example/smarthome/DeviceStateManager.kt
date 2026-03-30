package com.example.smarthome

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*
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
    private val deviceStartTime = mutableMapOf<String, Long>()
    private val deviceTotalTime = mutableMapOf<String, Long>()
    private val deviceUsageLimits = mutableMapOf<String, Double>()
    
    var notificationCount: Int = 3
    var areNotificationsCleared: Boolean = false
    private var lastPersistTime = 0L

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

                (data["deviceTotalTime"] as? Map<String, Long>)?.let {
                    deviceTotalTime.clear()
                    deviceTotalTime.putAll(it)
                }

                (data["deviceUsageLimits"] as? Map<String, Double>)?.let {
                    deviceUsageLimits.clear()
                    deviceUsageLimits.putAll(it)
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
            "deviceTotalTime" to deviceTotalTime,
            "deviceUsageLimits" to deviceUsageLimits,
            "notificationCount" to notificationCount,
            "areNotificationsCleared" to areNotificationsCleared
        )
        getUserDoc()?.set(data, SetOptions.merge())
    }

    fun setDeviceState(deviceId: String, isOn: Boolean) {
        val wasOn = getDeviceState(deviceId)
        if (isOn && !wasOn) {
            deviceStartTime[deviceId] = System.currentTimeMillis()
        } else if (!isOn && wasOn) {
            val startTime = deviceStartTime.remove(deviceId) ?: System.currentTimeMillis()
            val sessionTime = System.currentTimeMillis() - startTime
            val currentTotal = deviceTotalTime[deviceId] ?: 0L
            deviceTotalTime[deviceId] = currentTotal + sessionTime
        }
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
    fun getTotalLiveLoad(): Double {
        var total = 0.0
        if (getDeviceState("ac_living_room")) total += 1.5
        if (getDeviceState("fan_unit_1")) total += 0.2
        if (getDeviceState("lamp_bedroom")) total += 0.05
        if (getDeviceState("tv_living_room")) total += 0.3

        // Add small fluctuation for real-time feel
        if (total > 0) {
            total += Random.nextDouble(-0.02, 0.02)
        }
        return total.coerceAtLeast(0.0)
    }

    fun getCurrentInAmps(): Double {
        // P = V * I  => I = P / V (Assuming 230V)
        return (getTotalLiveLoad() * 1000) / 230.0
    }

    fun getUsageStatus(): String {
        val load = getTotalLiveLoad()
        return when {
            load == 0.0 -> "Ideal"
            load < 1.0 -> "Good"
            load < 2.0 -> "Moderate"
            else -> "High"
        }
    }

    fun getActiveTimeMs(deviceId: String): Long {
        val accumulated = deviceTotalTime[deviceId] ?: 0L
        val currentSession = if (getDeviceState(deviceId)) {
            val start = deviceStartTime[deviceId] ?: System.currentTimeMillis()
            System.currentTimeMillis() - start
        } else 0L
        return accumulated + currentSession
    }

    fun getTotalActiveTimeMinutes(): Long {
        var totalMs = 0L
        // Track all known devices for total time
        val allDevices = setOf("ac_living_room", "fan_unit_1", "lamp_bedroom", "tv_living_room") + deviceStates.keys
        allDevices.forEach { deviceId ->
            totalMs += getActiveTimeMs(deviceId)
        }
        return totalMs / 60000
    }

    fun getSimulatedRPM(deviceId: String): Int {
        if (!getDeviceState(deviceId)) return 0
        val speed = getDeviceValue(deviceId, 1)
        return (speed * 300) + Random.nextInt(-20, 20)
    }

    fun getEnergyUsage(deviceId: String): Double {
        return deviceUsage[deviceId] ?: (Random.nextDouble(0.5, 2.5))
    }

    fun getEnergyUsageFormatted(deviceId: String): String {
        return String.format("%.4f kWh", getEnergyUsage(deviceId))
    }

    fun simulateUsageIncrement() {
        var changed = false
        deviceStates.forEach { (id, isOn) ->
            if (isOn) {
                val current = deviceUsage[id] ?: (Random.nextDouble(0.5, 2.5))
                // Increment slightly (simulating real-time consumption)
                deviceUsage[id] = current + (Random.nextDouble(0.0001, 0.0003))
                changed = true
            }
        }

        // Occasionally persist to Firestore (every 30 seconds if anything changed)
        val now = System.currentTimeMillis()
        if (changed && now - lastPersistTime > 30000) {
            updateFirestore()
            lastPersistTime = now
        }
    }

    fun setUsageLimit(deviceId: String, limitKwh: Double) {
        deviceUsageLimits[deviceId] = limitKwh
        updateFirestore()
    }

    fun getUsageLimit(deviceId: String): Double = deviceUsageLimits[deviceId] ?: 0.0

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
