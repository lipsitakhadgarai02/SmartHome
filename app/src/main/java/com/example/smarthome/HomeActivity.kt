package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI and Navigation
        setupDrawer()
        setupBottomNavigation()
        setupClickListeners()
        handleBackNavigation()
        setupRealTimeDate()
        setupDynamicWeather()
        updateUserInfo()
        
        // Fix for 3-button navigation spacing and icon clipping
        applyWindowInsets()
    }

    override fun onResume() {
        super.onResume()
        syncDeviceStates()
        updateNotificationBadge()
        updateUserInfo()
    }

    private fun updateUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val tvUserName = findViewById<TextView>(R.id.tv_user_name)
            val name = currentUser.displayName ?: currentUser.email?.split("@")?.get(0) ?: "User"
            tvUserName?.text = name

            // Update Drawer Header
            val navigationView = findViewById<NavigationView>(R.id.navigation_view)
            val headerView = navigationView.getHeaderView(0)
            val tvHeaderName = headerView.findViewById<TextView>(R.id.tv_header_name)
            val tvHeaderEmail = headerView.findViewById<TextView>(R.id.tv_header_email)
            
            tvHeaderName?.text = name
            tvHeaderEmail?.text = currentUser.email ?: ""
        }
    }

    private fun updateNotificationBadge() {
        val badge = findViewById<TextView>(R.id.tv_notification_badge)
        val count = DeviceStateManager.notificationCount
        
        if (count > 0 && !DeviceStateManager.areNotificationsCleared) {
            badge?.text = count.toString()
            badge?.visibility = View.VISIBLE
        } else {
            badge?.visibility = View.GONE
        }
    }

    private fun syncDeviceStates() {
        val tvPaired = findViewById<View>(R.id.tv_paired)
        val parent = tvPaired?.parent as? ViewGroup
        val grid = parent?.getChildAt(parent.indexOfChild(tvPaired) + 1) as? ViewGroup
        
        if (grid != null) {
            val row1 = grid.getChildAt(0) as? ViewGroup
            val acCard = row1?.getChildAt(0) as? CardView
            val tvCard = row1?.getChildAt(1) as? CardView
            
            val row2 = grid.getChildAt(1) as? ViewGroup
            val lightsCard = row2?.getChildAt(0) as? CardView
            val fanCard = row2?.getChildAt(1) as? CardView

            updateDeviceUI("ac_living_room", acCard)
            updateDeviceUI("tv_living_room", tvCard)
            updateDeviceUI("lamp_bedroom", lightsCard)
            updateDeviceUI("fan_unit_1", fanCard)
        }
    }

    private fun updateDeviceUI(deviceId: String, card: CardView?) {
        val isPaired = DeviceStateManager.isDevicePaired(deviceId)
        card?.alpha = if (isPaired) 1.0f else 0.5f
    }

    private fun setupRealTimeDate() {
        val tvDate = findViewById<TextView>(R.id.tv_card_date)
        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
        tvDate?.text = sdf.format(Date())
    }

    private fun setupDynamicWeather() {
        val tvTemp = findViewById<TextView>(R.id.tv_home_temp)
        val tvHumidity = findViewById<TextView>(R.id.tv_home_humidity)

        val temp = Random.nextInt(20, 41)
        val humidity = Random.nextInt(30, 81)

        tvTemp?.text = "${temp}°"
        tvHumidity?.text = "Humidity: ${humidity}%"
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val ivMenu = findViewById<ImageView>(R.id.iv_menu)

        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_home -> {}
                R.id.drawer_devices -> {
                    startActivity(Intent(this, DeviceScanActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                }
                R.id.drawer_add_device -> {
                    startActivity(Intent(this, DeviceScanActivity::class.java).apply {
                        putExtra("SCAN_MODE", "QR_CODE")
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                }
                R.id.drawer_automations -> {
                    startActivity(Intent(this, DeviceManagementActivity::class.java).apply {
                        putExtra("FLOW_TYPE", "AUTOMATION")
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                }
                R.id.drawer_notifications -> navigateToNotifications()
                R.id.drawer_settings -> {
                    startActivity(Intent(this, UserProfileActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                }
                R.id.drawer_logout -> performLogout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.nav_home)?.setOnClickListener {}
        findViewById<View>(R.id.nav_search)?.setOnClickListener {
            startActivity(Intent(this, RoomsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }
        findViewById<View>(R.id.nav_devices)?.setOnClickListener {
            startActivity(Intent(this, DeviceScanActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }
        findViewById<View>(R.id.nav_settings)?.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cv_energy)?.setOnClickListener {
            startActivity(Intent(this, DeviceManagementActivity::class.java).apply {
                putExtra("FLOW_TYPE", "SENSOR_MONITORING")
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }

        findViewById<Button>(R.id.btn_go_devices)?.setOnClickListener {
            startActivity(Intent(this, DeviceScanActivity::class.java).apply {
                putExtra("SCAN_MODE", "MANUAL")
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }

        findViewById<View>(R.id.fl_notification_container)?.setOnClickListener {
            navigateToNotifications()
        }
        
        val tvPaired = findViewById<View>(R.id.tv_paired)
        val parent = tvPaired?.parent as? ViewGroup
        val grid = parent?.getChildAt(parent.indexOfChild(tvPaired) + 1) as? ViewGroup
        
        if (grid != null) {
            val row1 = grid.getChildAt(0) as? ViewGroup
            // AC
            row1?.getChildAt(0)?.setOnClickListener { 
                handleDeviceNavigation("ac_living_room", "Air Conditioner", AcControlActivity::class.java)
            }
            // TV
            row1?.getChildAt(1)?.setOnClickListener { 
                handleDeviceNavigation("tv_living_room", "Smart TV", TvRemoteActivity::class.java)
            }
            
            val row2 = grid.getChildAt(1) as? ViewGroup
            // Lamp
            row2?.getChildAt(0)?.setOnClickListener { 
                handleDeviceNavigation("lamp_bedroom", "Lamp", LampControlActivity::class.java)
            }
            // Fan
            row2?.getChildAt(1)?.setOnClickListener { 
                handleDeviceNavigation("fan_unit_1", "Smart Fan", FanControlActivity::class.java)
            }

            // Long click for unpairing
            row1?.getChildAt(0)?.setOnLongClickListener { showUnpairingDialog("Air Conditioner", "ac_living_room"); true }
            row1?.getChildAt(1)?.setOnLongClickListener { showUnpairingDialog("Smart TV", "tv_living_room"); true }
            row2?.getChildAt(0)?.setOnLongClickListener { showUnpairingDialog("Lamp", "lamp_bedroom"); true }
            row2?.getChildAt(1)?.setOnLongClickListener { showUnpairingDialog("Smart Fan", "fan_unit_1"); true }
        }
    }

    private fun handleDeviceNavigation(deviceId: String, name: String, target: Class<*>) {
        if (DeviceStateManager.isDevicePaired(deviceId)) {
            startActivity(Intent(this, target).apply {
                putExtra("DEVICE_ID", deviceId)
            })
        } else {
            showPairingDialog(name, deviceId)
        }
    }

    private fun showPairingDialog(deviceName: String, deviceId: String) {
        AlertDialog.Builder(this)
            .setTitle("Pair Device")
            .setMessage("Would you like to pair with $deviceName?")
            .setPositiveButton("Pair") { _, _ ->
                DeviceStateManager.pairDevice(deviceId)
                Toast.makeText(this, "$deviceName paired successfully!", Toast.LENGTH_SHORT).show()
                syncDeviceStates()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUnpairingDialog(deviceName: String, deviceId: String) {
        AlertDialog.Builder(this)
            .setTitle("Unpair Device")
            .setMessage("Are you sure you want to unpair $deviceName?")
            .setPositiveButton("Unpair") { _, _ ->
                DeviceStateManager.unpairDevice(deviceId)
                Toast.makeText(this, "$deviceName unpaired.", Toast.LENGTH_SHORT).show()
                syncDeviceStates()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyWindowInsets() {
        val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomNavContainer?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomInsets(systemBars.bottom)
            }
            insets
        }
    }

    private fun ViewGroup.MarginLayoutParams.bottomInsets(insetBottom: Int) {
        val density = resources.displayMetrics.density
        val baseMargin = (24 * density).toInt()
        bottomMargin = baseMargin + insetBottom
    }

    private fun handleBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    showExitDialog()
                }
            }
        })
    }

    private fun navigateToNotifications() {
        startActivity(Intent(this, AccountManagementActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }
}
