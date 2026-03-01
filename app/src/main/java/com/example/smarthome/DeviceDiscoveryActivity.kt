package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class DeviceDiscoveryActivity : AppCompatActivity() {
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var pbScanning: ProgressBar
    private lateinit var pbSmall: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var devicesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)
        
        initViews()
        setupUI()
        setupSwipeRefresh()
        
        // Start simulation immediately on entry
        simulateDiscovery()
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
            simulateDiscovery()
            handler.postDelayed({ swipeRefreshLayout.isRefreshing = false }, 1500)
        }
    }

    private fun simulateDiscovery() {
        // Show loading state
        devicesContainer.removeAllViews()
        pbScanning.visibility = View.VISIBLE
        pbSmall.visibility = View.VISIBLE
        tvStatus.text = "Scanning network..."

        // Simulating network delay for discovery
        handler.postDelayed({
            // Hide loading state
            pbScanning.visibility = View.GONE
            pbSmall.visibility = View.GONE
            tvStatus.text = "Connected to: SmartHome_WiFi"
            
            val scanMode = intent.getStringExtra("SCAN_MODE")
            if (scanMode == "Wi-Fi") {
                addDeviceItem("Smart TV (Living Room)", "Connected via Wi-Fi", R.drawable.ic_tv) {
                    connectToTv()
                }
            } else {
                addDeviceItem("Generic Smart Device", "Found nearby", R.drawable.ic_devices) {
                    Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show()
                }
            }
        }, 2000)
    }

    private fun connectToTv() {
        val deviceId = "tv_living_room"
        val deviceName = "Smart TV (Living Room)"
        
        Toast.makeText(this, "Connecting to TV via Wi-Fi...", Toast.LENGTH_SHORT).show()
        
        handler.postDelayed({
            DeviceStateManager.pairDevice(deviceId)
            Toast.makeText(this, "Connected to $deviceName", Toast.LENGTH_SHORT).show()
            
            val intent = Intent(this, TvRemoteActivity::class.java).apply {
                putExtra("DEVICE_ID", deviceId)
                putExtra("CONNECTION_MODE", "Wi-Fi")
            }
            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun addDeviceItem(name: String, subtext: String, iconRes: Int, onClick: () -> Unit) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_discovered_device, devicesContainer, false)
        
        val tvName = itemView.findViewById<TextView>(R.id.tv_device_name)
        val tvSubtext = itemView.findViewById<TextView>(R.id.tv_device_subtext)
        val ivIcon = itemView.findViewById<ImageView>(R.id.iv_device_icon)
        
        tvName.text = name
        tvSubtext.text = subtext
        ivIcon.setImageResource(iconRes)
        
        itemView.setOnClickListener { onClick() }

        devicesContainer.addView(itemView)
    }
}