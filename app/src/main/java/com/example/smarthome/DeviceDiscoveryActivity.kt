package com.example.smarthome

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * DeviceDiscoveryActivity handles real-time discovery of Smart TVs and other devices
 * using both Wi-Fi Scanning (for AP mode) and NSD (for devices on the same network).
 */
class DeviceDiscoveryActivity : AppCompatActivity() {
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var pbScanning: ProgressBar
    private lateinit var pbSmall: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var devicesContainer: LinearLayout
    private lateinit var wifiManager: WifiManager
    private lateinit var nsdManager: NsdManager
    
    private val discoveredDevices = mutableSetOf<String>()
    private var isScanning = false

    // NSD Discovery Listener for Smart TVs (Google Cast, DLNA, etc.)
    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d("NSD", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            runOnUiThread {
                val name = service.serviceName
                if (!discoveredDevices.contains(name)) {
                    discoveredDevices.add(name)
                    addDeviceItem(name, "Found on network (${service.serviceType})", R.drawable.ic_tv) {
                        connectToTv(name)
                    }
                    updateUIState()
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            runOnUiThread {
                // Optionally remove from list, but for now we keep it
            }
        }

        override fun onDiscoveryStopped(regType: String) {}
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("NSD", "Discovery failed: $errorCode")
            try { nsdManager.stopServiceDiscovery(this) } catch (e: Exception) {}
        }
        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
    }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val results = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                wifiManager.scanResults
            } else {
                emptyList<ScanResult>()
            }
            
            processWifiResults(results)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)
        
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        
        initViews()
        setupUI()
        setupSwipeRefresh()
        
        checkPermissionsAndStart()
    }

    private fun initViews() {
        pbScanning = findViewById(R.id.pb_scanning)
        pbSmall = findViewById(R.id.pb_small)
        tvStatus = findViewById(R.id.tv_category_label)
        devicesContainer = findViewById(R.id.ll_devices_container)
    }

    private fun setupUI() {
        findViewById<ImageView>(R.id.btn_back_discovery).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val scanMode = intent.getStringExtra("SCAN_MODE") ?: "Device"
        findViewById<TextView>(R.id.tv_discovery_title)?.text = "$scanMode Discovery"
    }

    private fun setupSwipeRefresh() {
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout?.setColorSchemeColors(getColor(R.color.primary_teal))
        swipeRefreshLayout?.setOnRefreshListener {
            startDiscovery()
            handler.postDelayed({ swipeRefreshLayout.isRefreshing = false }, 1500)
        }
    }

    private fun checkPermissionsAndStart() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 100)
        } else {
            startDiscovery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDiscovery()
        } else {
            tvStatus.text = "Permission denied. Cannot scan."
            pbScanning.visibility = View.GONE
            pbSmall.visibility = View.GONE
        }
    }

    private fun startDiscovery() {
        if (isScanning) return
        isScanning = true
        
        devicesContainer.removeAllViews()
        discoveredDevices.clear()
        
        pbScanning.visibility = View.VISIBLE
        pbSmall.visibility = View.VISIBLE
        tvStatus.text = "Scanning for devices..."

        // 1. Start Wi-Fi Scan (for AP Mode/Hotspots)
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, filter)
        wifiManager.startScan()

        // 2. Start NSD (for devices already on current Wi-Fi)
        try {
            nsdManager.discoverServices("_googlecast._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e("NSD", "Failed to start NSD", e)
        }

        // Auto-stop loading indicators after a timeout if nothing is found
        handler.postDelayed({
            if (discoveredDevices.isEmpty() && isScanning) {
                tvStatus.text = "No devices found. Ensure TV Wi-Fi is ON."
                pbScanning.visibility = View.GONE
                pbSmall.visibility = View.GONE
            }
        }, 10000)
    }

    private fun processWifiResults(results: List<ScanResult>) {
        runOnUiThread {
            for (result in results) {
                val ssid = if (result.SSID.isNullOrEmpty()) "Unknown Device" else result.SSID
                if (discoveredDevices.contains(ssid)) continue
                
                // Identify potential TVs by name
                val lowerSSID = ssid.lowercase()
                val isTv = lowerSSID.contains("tv") || lowerSSID.contains("smart") || lowerSSID.contains("display")
                
                if (isTv) {
                    discoveredDevices.add(ssid)
                    addDeviceItem(ssid, "Nearby Wi-Fi Hotspot", R.drawable.ic_tv) {
                        connectToTv(ssid)
                    }
                }
            }
            updateUIState()
        }
    }

    private fun updateUIState() {
        if (discoveredDevices.isNotEmpty()) {
            pbScanning.visibility = View.GONE
            pbSmall.visibility = View.GONE
            tvStatus.text = "Found ${discoveredDevices.size} devices nearby"
        }
    }

    private fun connectToTv(name: String) {
        val deviceId = "tv_" + name.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() }
        
        Toast.makeText(this, "Connecting to $name...", Toast.LENGTH_SHORT).show()
        
        // Simulate pairing process
        handler.postDelayed({
            DeviceStateManager.pairDevice(deviceId)
            Toast.makeText(this, "Successfully paired with $name", Toast.LENGTH_SHORT).show()
            
            val intent = Intent(this, TvRemoteActivity::class.java).apply {
                putExtra("DEVICE_ID", deviceId)
                putExtra("DEVICE_NAME", name)
                putExtra("CONNECTION_MODE", "Wi-Fi")
            }
            startActivity(intent)
            finish()
        }, 2000)
    }

    private fun addDeviceItem(name: String, subtext: String, iconRes: Int, onClick: () -> Unit) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_discovered_device, devicesContainer, false)
        itemView.findViewById<TextView>(R.id.tv_device_name).text = name
        itemView.findViewById<TextView>(R.id.tv_device_subtext).text = subtext
        itemView.findViewById<ImageView>(R.id.iv_device_icon).setImageResource(iconRes)
        itemView.setOnClickListener { onClick() }
        devicesContainer.addView(itemView)
    }

    override fun onStop() {
        super.onStop()
        stopDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDiscovery()
    }

    private fun stopDiscovery() {
        if (!isScanning) return
        isScanning = false
        try {
            unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {}
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {}
    }
}