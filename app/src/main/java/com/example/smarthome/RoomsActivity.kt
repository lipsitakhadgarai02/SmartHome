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

        // Setup click listeners for other static rooms and fix device counts
        val bedRoomCard = gridLayout.getChildAt(1) as? ViewGroup
        updateStaticDeviceCount(bedRoomCard, "1 Device")
        bedRoomCard?.setOnClickListener { navigateToRoomDetails("Bed Room") }

        val kitchenCard = gridLayout.getChildAt(2) as? ViewGroup
        updateStaticDeviceCount(kitchenCard, "1 Device")
        kitchenCard?.setOnClickListener { navigateToRoomDetails("Kitchen") }

        val livingRoomCard = gridLayout.getChildAt(3) as? ViewGroup
        updateStaticDeviceCount(livingRoomCard, "1 Device")
        livingRoomCard?.setOnClickListener { navigateToRoomDetails("Living Room") }

        // Inflate and add newly created rooms from DeviceStateManager
        val inflater = LayoutInflater.from(this)
        DeviceStateManager.getRooms().forEach { roomName ->
            // Avoid duplicates if refresh is called multiple times
            if (roomList.none { it.second == roomName }) {
                addNewRoomCard(gridLayout, inflater, roomName)
            }
        }
    }

    private fun updateStaticDeviceCount(card: ViewGroup?, text: String) {
        val linearLayout = card?.getChildAt(0) as? ViewGroup
        val tvCount = linearLayout?.getChildAt(3) as? TextView
        tvCount?.text = text
    }

    private fun addNewRoomCard(gridLayout: GridLayout, inflater: LayoutInflater, roomName: String) {
        val roomCard = inflater.inflate(R.layout.item_room_card, gridLayout, false)
        
        // Set Data
        roomCard.findViewById<TextView>(R.id.tv_room_name).text = roomName
        roomCard.findViewById<TextView>(R.id.tv_room_category).text = "Custom Room"
        
        // Count active accessories
        val devices = DeviceStateManager.getDevicesInRoom(roomName)
        val activeCount = devices.size
        roomCard.findViewById<TextView>(R.id.tv_device_count).text = "$activeCount Device${if (activeCount != 1) "s" else ""}"

        // Set Tag for filtering
        val container = roomCard.findViewById<View>(R.id.ll_room_container)
        container.tag = roomName

        // Click logic (Show details for custom rooms)
        roomCard.setOnClickListener {
            navigateToRoomDetails(roomName)
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
                val devices = mutableListOf<String>()
                if (switchAc.isChecked) devices.add("AC")
                if (switchLight.isChecked) devices.add("Light")
                if (switchFan.isChecked) devices.add("Fan")
                
                DeviceStateManager.addRoom(roomName, devices)
                
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

    private fun navigateToRoomDetails(roomName: String) {
        startActivity(Intent(this, RoomDetailsActivity::class.java).apply {
            putExtra("ROOM_NAME", roomName)
        })
    }
}