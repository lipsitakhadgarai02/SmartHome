package com.example.smarthome

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class DeviceScanActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var pendingScanType: String? = null

    private val requestBtPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            checkBluetoothAndScan()
        } else {
            Toast.makeText(this, "Bluetooth permissions required", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestWifiPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            startRealWifiScan()
        } else {
            Toast.makeText(this, "Location permission required for Wi-Fi scan", Toast.LENGTH_SHORT).show()
        }
    }

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startRealBluetoothScan()
        } else {
            Toast.makeText(this, "Bluetooth must be enabled to scan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        setupNavigation()
        setupScanningActions()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupScanningActions() {
        // Real Wi-Fi Scan
        findViewById<CardView>(R.id.cv_scan_nearby)?.setOnClickListener {
            handleWifiScanRequest()
        }

        // Real Bluetooth Scan for Buds/TWS
        findViewById<CardView>(R.id.cv_scan_bluetooth)?.setOnClickListener {
            pendingScanType = "BUDS"
            handleBluetoothScanRequest()
        }

        // Bluetooth Scan for Speakers
        val grid = findViewById<GridLayout>(R.id.gl_categories)
        grid?.getChildAt(4)?.setOnClickListener {
            pendingScanType = "SPEAKER"
            handleBluetoothScanRequest()
        }
    }

    private fun handleWifiScanRequest() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Please turn on Wi-Fi first", Toast.LENGTH_SHORT).show()
            return
        }

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startRealWifiScan()
        } else {
            requestWifiPermissionLauncher.launch(permissions)
        }
    }

    private fun startRealWifiScan() {
        Toast.makeText(this, "Scanning local Wi-Fi network...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DeviceDiscoveryActivity::class.java).apply {
            putExtra("SCAN_MODE", "Wi-Fi")
        }
        startActivity(intent)
    }

    private fun handleBluetoothScanRequest() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            checkBluetoothAndScan()
        } else {
            requestBtPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun checkBluetoothAndScan() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        } else {
            startRealBluetoothScan()
        }
    }

    private fun startRealBluetoothScan() {
        val scanType = pendingScanType ?: "Bluetooth"
        Toast.makeText(this, "Starting $scanType scan...", Toast.LENGTH_SHORT).show()
        
        val intent = Intent(this, DeviceDiscoveryActivity::class.java).apply {
            putExtra("SCAN_MODE", "Bluetooth")
            putExtra("SCAN_TYPE", scanType)
        }
        startActivity(intent)
    }
}
