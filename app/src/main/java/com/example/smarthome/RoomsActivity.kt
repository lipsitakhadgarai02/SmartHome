package com.example.smarthome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener

/**
 * RoomsActivity handles the Room List and searching functionality.
 */
class RoomsActivity : AppCompatActivity() {

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var roomList: List<Pair<View, String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupNavigation()
        setupSearch()
        setupRoomClickListeners()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
    }

    private fun setupSearch() {
        val searchBar = findViewById<View>(R.id.search_bar)
        val editText = findViewById<EditText>(R.id.et_search)

        // Fixed "laggy" feel by making search enable on first tap instead of three
        searchBar?.setOnClickListener {
            enableSearch(editText)
        }

        editText?.addTextChangedListener { text ->
            // Debounce search to reduce typing lag
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
            setCursorVisible(true) // Fixed: Unresolved reference 'cursorVisible' by using explicit setter
            requestFocus()
            
            // Show keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun filterRooms(query: String) {
        // Cache rooms to optimize filtering performance and reduce UI lag
        if (roomList == null) {
            val gridLayout = findViewById<GridLayout>(R.id.gl_rooms)
            roomList = gridLayout?.children?.mapNotNull { card ->
                val layout = (card as? ViewGroup)?.children?.firstOrNull { it is ViewGroup }
                val name = layout?.tag as? String
                if (name != null) card to name else null
            }?.toList()
        }

        roomList?.forEach { (card, name) ->
            card.visibility = if (name.contains(query, ignoreCase = true) || query.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupRoomClickListeners() {
        val gridLayout = findViewById<GridLayout>(R.id.gl_rooms)
        gridLayout?.let { grid ->
            // Index 0 is Create Room, others are Bed, Kitchen, Living
            grid.getChildAt(1)?.setOnClickListener { navigateToDevice("Lamp", LampControlActivity::class.java, "Bed Room") }
            grid.getChildAt(2)?.setOnClickListener { navigateToDevice("AC", AcControlActivity::class.java, "Kitchen") }
            grid.getChildAt(3)?.setOnClickListener { navigateToDevice("TV", TvRemoteActivity::class.java, "Living Room") }
        }
    }

    private fun navigateToDevice(type: String, target: Class<*>, roomName: String) {
        startActivity(Intent(this, target).apply {
            putExtra("DEVICE_TYPE", type)
            putExtra("ROOM_NAME", roomName)
        })
    }
}