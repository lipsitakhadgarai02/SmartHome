package com.example.smarthome

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MqttHandler(private val context: Context, private val brokerIp: String) {

    private var mqttClient: MqttClient? = null
    private val brokerUrl = "tcp://$brokerIp:1883"
    private val clientId = "AndroidClient_${System.currentTimeMillis()}"
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        try {
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
            connect()
        } catch (e: MqttException) {
            Log.e("MQTT", "Initialization failed: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun connect() {
        scope.launch {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 20
            }

            try {
                if (mqttClient?.isConnected == false) {
                    mqttClient?.connect(options)
                    Log.d("MQTT", "Connected to $brokerUrl")
                    showToast("Connected to Laptop at $brokerIp")
                }
            } catch (e: MqttException) {
                Log.e("MQTT", "Connection failed: ${e.message}")
                showToast("Connection Failed: Check IP $brokerIp")
            }
        }
    }

    fun publish(topic: String, message: String) {
        scope.launch {
            try {
                if (mqttClient?.isConnected == false) {
                    connect()
                }
                
                if (mqttClient?.isConnected == true) {
                    val mqttMessage = MqttMessage(message.toByteArray())
                    mqttMessage.qos = 1
                    mqttClient?.publish(topic, mqttMessage)
                    Log.d("MQTT", "Published: $message")
                }
            } catch (e: MqttException) {
                Log.e("MQTT", "Publish failed: ${e.message}")
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                mqttClient?.disconnect()
            } catch (e: Exception) {
                Log.e("MQTT", "Disconnect error")
            }
        }
    }
}