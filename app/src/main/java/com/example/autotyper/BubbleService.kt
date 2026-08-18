package com.example.autotyper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast

class BubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var panelView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addBubble()
    }

    private fun startForegroundWithNotification() {
        val channelId = "autotyper_bubble"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "AutoTyper Bubble", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("AutoTyper is running")
            .setContentText("Tap the floating bubble to insert a snippet")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .build()

        startForeground(1, notification)
    }

    private fun addBubble() {
        val button = Button(this).apply {
            text = "T"
            setOnClickListener { togglePanel() }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        button.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, params)
                    false
                }
                else -> false
            }
        }

        bubbleView = button
        windowManager.addView(button, params)
    }

    private fun togglePanel() {
        if (panelView != null) {
            removePanel()
            return
        }

        val store = SnippetStore(this)
        val snippets = store.getAll()

        val listView = ListView(this).apply {
            adapter = ArrayAdapter(
                this@BubbleService,
                android.R.layout.simple_list_item_1,
                snippets.map { it.name }
            )
            setOnItemClickListener { _, _, position, _ ->
                val snippet = snippets[position]
                val service = TyperAccessibilityService.instance
                if (service == null) {
                    Toast.makeText(
                        this@BubbleService,
                        "Accessibility service not enabled",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val ok = service.typeText(snippet.text)
                    if (!ok) {
                        Toast.makeText(
                            this@BubbleService,
                            "No editable field is focused",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                removePanel()
            }
        }

        val params = WindowManager.LayoutParams(
            600,
            800,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        panelView = listView
        windowManager.addView(listView, params)
    }

    private fun removePanel() {
        panelView?.let { windowManager.removeView(it) }
        panelView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        bubbleView?.let { windowManager.removeView(it) }
        removePanel()
    }
}
