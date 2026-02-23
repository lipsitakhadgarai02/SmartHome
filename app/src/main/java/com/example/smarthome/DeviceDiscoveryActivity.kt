package com.example.smarthome

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.net.InetAddress
import java.util.concurrent.Executors

class DeviceDiscoveryActivity : AppCompatActivity() {
    
    private var scanMode: String? = null
    private var scanType: String? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothA2dp: BluetoothA2dp? = null
    private val discoveredDevices = mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private var refreshRunnable: Runnable? = null
    private val scanExecutor = Executors.newFixedThreadPool(10)

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = proxy as BluetoothA2dp
            }
        }
        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = null
            }
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!discoveredDevices.contains(it.address) && isMatchingScanType(it)) {
                        discoveredDevices.add(it.address)
                        addBluetoothDeviceToUI(it)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)

        scanMode = intent.getStringExtra("SCAN_MODE")
        scanType = intent.getStringExtra("SCAN_TYPE")
        
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        setupUI()
        setupSwipeRefresh()
        
        if (scanMode == "Bluetooth") {
            bluetoothAdapter?.getProfileProxy(this, profileListener, BluetoothProfile.A2DP)
            startBluetoothDiscovery()
        } else if (scanMode == "Wi-Fi") {
            startWifiDiscovery()
        }
        
        setupAutoRefresh()
    }

    private fun setupUI() {
        findViewById<ImageView>(R.id.btn_back_discovery).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_refresh_manual)?.setOnClickListener {
            triggerRefresh()
        }

        val title = when(scanMode) {
            "Bluetooth" -> if (scanType == "BUDS") "Nearby Buds & TWS" else "Nearby Speakers"
            "Wi-Fi" -> "Nearby Wi-Fi Devices"
            else -> "Discovery Mode"
        }
        findViewById<TextView>(R.id.tv_discovery_title)?.text = title
        findViewById<TextView>(R.id.tv_category_label)?.text = "Available on $scanMode"
    }

    private fun setupSwipeRefresh() {
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout?.setColorSchemeColors(getColor(R.color.primary_teal))
        swipeRefreshLayout?.setOnRefreshListener {
            triggerRefresh()
            // Stop the loading spinner after discovery starts
            handler.postDelayed({
                swipeRefreshLayout.isRefreshing = false
            }, 1500)
        }
    }

    private fun triggerRefresh() {
        Toast.makeText(this, "Refreshing list...", Toast.LENGTH_SHORT).show()
        if (scanMode == "Bluetooth") {
            restartBluetoothDiscovery()
        } else if (scanMode == "Wi-Fi") {
            startWifiDiscovery()
        }
    }

    private fun setupAutoRefresh() {
        refreshRunnable = Runnable {
            if (scanMode == "Bluetooth") {
                restartBluetoothDiscovery()
            } else if (scanMode == "Wi-Fi") {
                startWifiDiscovery()
            }
            handler.postDelayed(refreshRunnable!!, 15000)
        }
        handler.postDelayed(refreshRunnable!!, 15000)
    }

    private fun restartBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.cancelDiscovery()
            discoveredDevices.clear()
            findViewById<LinearLayout>(R.id.ll_devices_container)?.removeAllViews()
            bluetoothAdapter?.startDiscovery()
        }
    }

    private fun startBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) return
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    private fun startWifiDiscovery() {
        findViewById<LinearLayout>(R.id.ll_devices_container)?.removeAllViews()
        discoveredDevices.clear()
        
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo.ssid.replace("\"", "")
        
        if (ssid == "<unknown ssid>") {
            Toast.makeText(this, "Not connected to Wi-Fi", Toast.LENGTH_SHORT).show()
            return
        }

        runOnUiThread {
            findViewById<TextView>(R.id.tv_category_label)?.text = "Network: $ssid"
        }

        val ipAddress = Formatter.formatIpAddress(wifiInfo.ipAddress)
        val prefix = ipAddress.substring(0, ipAddress.lastIndexOf(".") + 1)
        
        Toast.makeText(this, "Scanning $ssid...", Toast.LENGTH_SHORT).show()

        for (i in 1..254) {
            scanExecutor.execute {
                val testIp = prefix + i
                val address = InetAddress.getByName(testIp)
                if (address.isReachable(500)) {
                    val canonicalName = address.canonicalHostName
                    val hostName = address.hostName
                    
                    val deviceName = when {
                        canonicalName != testIp -> canonicalName
                        hostName != testIp -> hostName
                        else -> "Smart Device"
                    }
                    
                    val category = when {
                        deviceName.contains("phone", true) || deviceName.contains("android", true) || deviceName.contains("iphone", true) -> "Mobile Phone"
                        deviceName.contains("laptop", true) || deviceName.contains("desktop", true) || deviceName.contains("pc", true) -> "Computer/Laptop"
                        deviceName.contains("tv", true) -> "Smart TV"
                        deviceName.contains("ac", true) -> "Air Conditioner"
                        else -> "IoT Hardware"
                    }
                    
                    if (!discoveredDevices.contains(testIp)) {
                        discoveredDevices.add(testIp)
                        runOnUiThread { addWifiDeviceToUI("$deviceName ($category)", testIp) }
                    }
                }
            }
        }
    }

    private fun addBluetoothDeviceToUI(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
        val name = device.name ?: "Bluetooth Device"
        
        val deviceClass = device.bluetoothClass?.deviceClass ?: -1
        val category = when (deviceClass) {
            BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET,
            BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> "Buds/Headphones"
            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> "Bluetooth Speaker"
            BluetoothClass.Device.PHONE_SMART -> "Mobile Phone"
            BluetoothClass.Device.COMPUTER_LAPTOP -> "Laptop"
            else -> "Bluetooth Device"
        }

        val iconRes = if (scanType == "BUDS") R.drawable.ic_play else R.drawable.ic_speaker
        addDeviceItem("$name ($category)", "Handshake Available", iconRes) {
            handleBluetoothConnection(device, name)
        }
    }

    private fun addWifiDeviceToUI(displayName: String, ip: String) {
        val iconRes = when {
            displayName.contains("TV", true) -> R.drawable.ic_tv
            displayName.contains("AC", true) -> R.drawable.ic_ac
            displayName.contains("Phone", true) -> R.drawable.ic_profile
            else -> R.drawable.ic_launcher_foreground
        }
        
        addDeviceItem(displayName, "Found on local network", iconRes) {
            Toast.makeText(this, "Pairing with $displayName via Wi-Fi...", Toast.LENGTH_SHORT).show()
            pairAndNavigate(ip, AcControlActivity::class.java, displayName)
        }
    }

    private fun addDeviceItem(name: String, subtext: String, iconRes: Int, onClick: () -> Unit) {
        val container = findViewById<LinearLayout>(R.id.ll_devices_container) ?: return
        val itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false)
        
        val text1 = itemView.findViewById<TextView>(android.R.id.text1)
        val text2 = itemView.findViewById<TextView>(android.R.id.text2)
        
        text1.text = "  ▶  $name"
        text1.setTextColor(getColor(R.color.black))
        text1.typeface = Typeface.DEFAULT_BOLD
        
        text2.text = "      $subtext"
        text2.setTextColor(getColor(R.color.gray_text))
        
        itemView.setPadding(48, 32, 48, 32)
        itemView.setOnClickListener { onClick() }

        container.addView(itemView)
        
        val divider = View(this)
        divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        divider.setBackgroundColor(getColor(android.R.color.darker_gray))
        divider.alpha = 0.2f
        container.addView(divider)
    }

    private fun handleBluetoothConnection(device: BluetoothDevice, name: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            device.createBond()
        }
        pairAndNavigate(device.address, SpeakerControlActivity::class.java, name)
    }

    private fun isMatchingScanType(device: BluetoothDevice): Boolean {
        val deviceClass = device.bluetoothClass?.deviceClass ?: return true
        return when (scanType) {
            "BUDS" -> deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET || 
                      deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES
            "SPEAKER" -> deviceClass == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER
            else -> true
        }
    }

    private fun pairAndNavigate(deviceId: String, targetActivity: Class<*>, deviceName: String) {
        DeviceStateManager.pairDevice(deviceId)
        Toast.makeText(this, "$deviceName Connected!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, targetActivity).apply {
            putExtra("DEVICE_ID", deviceId)
            putExtra("DEVICE_NAME", deviceName)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshRunnable?.let { handler.removeCallbacks(it) }
        scanExecutor.shutdownNow()
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp)
        if (scanMode == "Bluetooth") {
            try { unregisterReceiver(bluetoothReceiver) } catch (e: Exception) {}
        }
    }
}
