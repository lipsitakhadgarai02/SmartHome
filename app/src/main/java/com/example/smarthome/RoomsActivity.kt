package com.example.smarthome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener

/**
 * RoomsActivity handles the Room List and searching functionality.
 */
class RoomsActivity : AppCompatActivity() {

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var roomList: MutableList<Pair<View, String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupNavigation()
        setupSearch()
        refreshRoomList()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSearch() {
        val searchBar = findViewById<View>(R.id.search_bar)
        val editText = findViewById<EditText>(R.id.et_search)

        searchBar?.setOnClickListener {
            enableSearch(editText)
        }

        editText?.addTextChangedListener { text ->
            searchRunnable?.let { searchHandler.removeCallbacks(it) }
            searchRunnable = Runnable {
                filterRooms(text.toString())
            }
            searchHandler.postDelayed(searchRunnable!!, 300)
        }
    }

    private fun enableSearch(editText: EditText?) {
        editText?.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            setCursorVisible(true)
            requestFocus()
            
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun filterRooms(query: String) {
        roomList.forEach { (card, name) ->
            card.visibility = if (name.contains(query, ignoreCase = true) || query.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun refreshRoomList() {
        val gridLayout = findViewById<GridLayout>(R.id.gl_rooms)
        
        // Identify static cards to preserve them in our logical list
        roomList.clear()
        
        // Add existing static cards from XML to the filter list
        gridLayout.children.forEach { card ->
            val layout = (card as? ViewGroup)?.children?.firstOrNull { it is ViewGroup }
            val name = layout?.tag as? String ?: ""
            if (name.isNotEmpty() || card.id == R.id.cv_create_room) {
                roomList.add(card to name)
            }
        }

        // Setup click listener for the static "Create Room" card (Index 0)
        findViewById<View>(R.id.cv_create_room)?.setOnClickListener {
            showCreateRoomDialog()
        }

        // Setup click listeners for other static rooms
        gridLayout.getChildAt(1)?.setOnClickListener { navigateToDevice("Lamp", LampControlActivity::class.java, "Bed Room") }
        gridLayout.getChildAt(2)?.setOnClickListener { navigateToDevice("AC", AcControlActivity::class.java, "Kitchen") }
        gridLayout.getChildAt(3)?.setOnClickListener { navigateToDevice("TV", TvRemoteActivity::class.java, "Living Room") }

        // Inflate and add newly created rooms from DeviceStateManager
        val inflater = LayoutInflater.from(this)
        DeviceStateManager.getRooms().forEach { roomName ->
            // Avoid duplicates if refresh is called multiple times
            if (roomList.none { it.second == roomName }) {
                addNewRoomCard(gridLayout, inflater, roomName)
            }
        }
    }

    private fun addNewRoomCard(gridLayout: GridLayout, inflater: LayoutInflater, roomName: String) {
        val roomCard = inflater.inflate(R.layout.item_room_card, gridLayout, false)
        
        // Set Data
        roomCard.findViewById<TextView>(R.id.tv_room_name).text = roomName
        roomCard.findViewById<TextView>(R.id.tv_room_category).text = "Custom Room"
        
        // Count active accessories
        var activeCount = 0
        if (DeviceStateManager.getDeviceState("${roomName}_AC")) activeCount++
        if (DeviceStateManager.getDeviceState("${roomName}_Light")) activeCount++
        if (DeviceStateManager.getDeviceState("${roomName}_Fan")) activeCount++
        roomCard.findViewById<TextView>(R.id.tv_device_count).text = "$activeCount Devices"

        // Set Tag for filtering
        val container = roomCard.findViewById<View>(R.id.ll_room_container)
        container.tag = roomName

        // Click logic (General remote for custom rooms)
        roomCard.setOnClickListener {
            navigateToDevice("Remote", TvRemoteActivity::class.java, roomName)
        }

        // Add to UI
        gridLayout.addView(roomCard)
        
        // Add to filter list
        roomList.add(roomCard to roomName)
    }

    private fun showCreateRoomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etRoomName = dialogView.findViewById<EditText>(R.id.et_room_name)
        val switchAc = dialogView.findViewById<SwitchCompat>(R.id.switch_ac)
        val switchLight = dialogView.findViewById<SwitchCompat>(R.id.switch_light)
        val switchFan = dialogView.findViewById<SwitchCompat>(R.id.switch_fan)
        val btnCreate = dialogView.findViewById<Button>(R.id.btn_create)

        btnCreate.setOnClickListener {
            val roomName = etRoomName.text.toString().trim()
            if (roomName.isNotEmpty()) {
                DeviceStateManager.addRoom(roomName)
                DeviceStateManager.setDeviceState("${roomName}_AC", switchAc.isChecked)
                DeviceStateManager.setDeviceState("${roomName}_Light", switchLight.isChecked)
                DeviceStateManager.setDeviceState("${roomName}_Fan", switchFan.isChecked)

                Toast.makeText(this, "Room '$roomName' created successfully!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                
                // Trigger UI refresh to show the new card immediately
                refreshRoomList()
            } else {
                etRoomName.error = "Please enter a name for the room"
            }
        }

        dialog.show()
    }

    private fun navigateToDevice(type: String, target: Class<*>, roomName: String) {
        startActivity(Intent(this, target).apply {
            putExtra("DEVICE_TYPE", type)
            putExtra("ROOM_NAME", roomName)
        })
    }
}