package com.example.autotyper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var store: SnippetStore
    private lateinit var listView: ListView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = SnippetStore(this)
        listView = findViewById(R.id.snippetList)
        statusText = findViewById(R.id.statusText)

        val inputName = findViewById<EditText>(R.id.inputName)
        val inputText = findViewById<EditText>(R.id.inputText)

        findViewById<Button>(R.id.btnAddSnippet).setOnClickListener {
            val name = inputName.text.toString().trim()
            val text = inputText.text.toString()
            if (name.isEmpty() || text.isEmpty()) {
                Toast.makeText(this, "Enter both a name and text", Toast.LENGTH_SHORT).show()
            } else {
                store.add(Snippet(name, text))
                inputName.text.clear()
                inputText.text.clear()
                refreshList()
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            store.delete(position)
            refreshList()
            true
        }

        findViewById<Button>(R.id.btnEnableAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnEnableOverlay).setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnStartBubble).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Grant the overlay permission first", Toast.LENGTH_SHORT).show()
            } else {
                startService(Intent(this, BubbleService::class.java))
                Toast.makeText(this, "Bubble started", Toast.LENGTH_SHORT).show()
            }
        }

        refreshList()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val accessibilityOn = TyperAccessibilityService.instance != null
        val overlayOn = Settings.canDrawOverlays(this)
        statusText.text = when {
            accessibilityOn && overlayOn -> "Ready - you can start the bubble."
            !accessibilityOn -> "Step 1 needed: turn on AutoTyper under Accessibility settings."
            else -> "Step 2 needed: grant the overlay permission."
        }
    }

    private fun refreshList() {
        val snippets = store.getAll()
        val labels = snippets.map { it.name }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
    }
}
