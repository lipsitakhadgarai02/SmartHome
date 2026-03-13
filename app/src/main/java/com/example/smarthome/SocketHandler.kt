package com.example.smarthome

import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class SocketHandler(private val onResponse: (String) -> Unit) {
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var listenerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Creates a persistent TCP connection to the laptop
     */
    fun connect(ip: String, port: Int = 5000) {
        scope.launch {
            try {
                // If already connected and working, don't create a new one
                if (socket != null && socket!!.isConnected && !socket!!.isClosed) return@launch

                Log.d("SOCKET_STATUS", "Attempting connection to $ip:$port")
                socket = Socket()
                socket?.connect(InetSocketAddress(ip, port), 5000)
                outputStream = socket?.getOutputStream()
                
                // Start background thread to listen for responses
                startListening()
                
                Log.d("SOCKET_STATUS", "Connected successfully")
            } catch (e: Exception) {
                Log.e("SOCKET_ERROR", "Connection failed: ${e.message}")
            }
        }
    }

    private fun startListening() {
        listenerJob = scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
                while (socket?.isConnected == true && !socket!!.isClosed) {
                    val response = reader.readLine()
                    if (response != null) {
                        Log.d("DEVICE_RESPONSE", response)
                        withContext(Dispatchers.Main) {
                            onResponse(response)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SOCKET_ERROR", "Listener thread closed: ${e.message}")
            }
        }
    }

    /**
     * Sends a command as JSON followed by a newline character
     */
    fun sendCommand(command: String) {
        scope.launch {
            try {
                if (socket != null && socket!!.isConnected && !socket!!.isClosed) {
                    val json = "{\"action\":\"$command\"}\n"
                    outputStream?.write(json.toByteArray())
                    outputStream?.flush()
                    Log.d("COMMAND_SENT", command)
                } else {
                    Log.e("COMMAND_ERROR", "Cannot send $command: Socket not connected")
                }
            } catch (e: Exception) {
                Log.e("COMMAND_ERROR", "Send failed: ${e.message}")
            }
        }
    }

    fun closeConnection() {
        listenerJob?.cancel()
        try {
            socket?.close()
        } catch (e: Exception) { }
    }
}