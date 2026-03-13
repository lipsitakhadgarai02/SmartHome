package com.example.smarthome

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale

class DeviceDiscoveryActivity : AppCompatActivity() {

    private lateinit var pbScanning: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var devicesContainer: LinearLayout
    private val discoveredDevices = mutableSetOf<String>()
    private var scanJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)

        initViews()
        setupUI()
        checkPermissionsAndStart()
    }

    private fun initViews() {
        pbScanning = findViewById(R.id.pb_scanning)
        tvStatus = findViewById(R.id.tv_category_label)
        devicesContainer = findViewById(R.id.ll_devices_container)
    }

    private fun setupUI() {
        findViewById<ImageView>(R.id.btn_back_discovery).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Make the status text clickable as a fallback for manual entry
        tvStatus.setOnClickListener {
            showManualIpDialog()
        }

        findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)?.setOnRefreshListener {
            startNetworkScan()
            findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)?.isRefreshing = false
        }
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            startNetworkScan()
        }
    }

    private fun startNetworkScan() {
        scanJob?.cancel()
        devicesContainer.removeAllViews()
        discoveredDevices.clear()
        pbScanning.visibility = View.VISIBLE
        tvStatus.text = "Scanning network for Laptop..."

        scanJob = lifecycleScope.launch(Dispatchers.IO) {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            
            if (ipAddress == 0) {
                withContext(Dispatchers.Main) {
                    tvStatus.text = "WiFi not connected. Tap to enter IP."
                    pbScanning.visibility = View.GONE
                }
                return@launch
            }

            val baseIp = String.format(Locale.US, "%d.%d.%d.",
                ipAddress and 0xff,
                (ipAddress shr 8) and 0xff,
                (ipAddress shr 16) and 0xff)

            // Scan 1..254 in parallel
            val jobs = (1..254).map { i ->
                async {
                    val host = baseIp + i
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(host, 5000), 700) // 700ms timeout for parallel reliability
                        withContext(Dispatchers.Main) {
                            addDeviceToList(host)
                        }
                        socket.close()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            jobs.awaitAll()

            withContext(Dispatchers.Main) {
                pbScanning.visibility = View.GONE
                if (discoveredDevices.isEmpty()) {
                    tvStatus.text = "No devices found. Tap here for manual IP."
                } else {
                    tvStatus.text = "Scan complete. Found ${discoveredDevices.size} devices."
                }
            }
        }
    }

    private fun showManualIpDialog() {
        val input = EditText(this)
        input.hint = "e.g. 192.168.1.15"
        
        AlertDialog.Builder(this)
            .setTitle("Manual Connection")
            .setMessage("Enter your Laptop's IP address:")
            .setView(input)
            .setPositiveButton("Connect") { _, _ ->
                val ip = input.text.toString().trim()
                if (ip.isNotEmpty()) {
                    connectToLaptop(ip)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addDeviceToList(host: String) {
        if (discoveredDevices.contains(host)) return
        discoveredDevices.add(host)

        val itemView = LayoutInflater.from(this).inflate(R.layout.item_discovered_device, devicesContainer, false)
        itemView.findViewById<TextView>(R.id.tv_device_name).text = "Laptop Discovered"
        itemView.findViewById<TextView>(R.id.tv_device_subtext).text = host
        itemView.findViewById<ImageView>(R.id.iv_device_icon).setImageResource(R.drawable.ic_tv)
        
        itemView.setOnClickListener {
            connectToLaptop(host)
        }
        devicesContainer.addView(itemView)
    }

    private fun connectToLaptop(ip: String) {
        val intent = Intent(this, TvRemoteActivity::class.java).apply {
            putExtra("DEVICE_IP", ip)
            putExtra("DEVICE_NAME", "Laptop Control")
            putExtra("CONNECTION_MODE", "SOCKET")
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanJob?.cancel()
    }
}
