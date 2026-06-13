package com.adhkarapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.adhkarapp.R
import com.adhkarapp.util.TtsHelper

class DhikrOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var dismissRunnable: Runnable? = null
    private var isShowing = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val dhikrText = intent?.getStringExtra("dhikr_text") ?: return START_NOT_STICKY
        val dhikrSource = intent.getStringExtra("dhikr_source") ?: ""
        val displaySeconds = intent.getIntExtra("display_seconds", 10)
        val playAudio = intent.getBooleanExtra("play_audio", false)
        val audioRepeat = intent.getIntExtra("audio_repeat", 1)

        if (!isShowing) {
            showOverlay(dhikrText, dhikrSource, displaySeconds, playAudio, audioRepeat)
        }

        startForegroundIfNeeded()
        return START_STICKY
    }

    private fun showOverlay(
        text: String, source: String, seconds: Int,
        playAudio: Boolean, audioRepeat: Int
    ) {
        isShowing = true
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dhikr_overlay, null)
        val textView = view.findViewById<TextView>(R.id.dhikr_text)
        val sourceView = view.findViewById<TextView>(R.id.dhikr_source)

        textView.text = text
        sourceView.text = if (source.isNotEmpty()) "— $source" else ""

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(0x33000000.toInt())
            cornerRadius = 0f
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                dismissOverlay(view)
                true
            } else false
        }

        try {
            windowManager.addView(view, params)

            if (playAudio) {
                TtsHelper.init(this) {
                    TtsHelper.speak(text, audioRepeat)
                }
            }

            val totalDelay = if (playAudio)
                (seconds * 1000L) + (text.length * 80L).coerceAtMost(15_000L)
            else seconds * 1000L

            dismissRunnable = Runnable { dismissOverlay(view) }
            handler.postDelayed(dismissRunnable!!, totalDelay)
        } catch (_: Exception) {
            isShowing = false
        }
    }

    private fun dismissOverlay(view: View) {
        if (!isShowing) return
        isShowing = false
        dismissRunnable?.let { handler.removeCallbacks(it) }
        TtsHelper.stop()
        if (view.isAttachedToWindow) {
            try { windowManager.removeView(view) } catch (_: Exception) {}
        }
        stopSelf()
    }

    private var isForeground = false

    private fun startForegroundIfNeeded() {
        if (!isForeground) {
            isForeground = true
            val channelId = "dhikr_overlay_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId, "عرض الأذكار",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { setSound(null, null) }
                val nm = getSystemService(NotificationManager::class.java)
                nm.createNotificationChannel(channel)
            }
            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("أذكاري")
                .setContentText("عرض الذكر")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            startForeground(200, notification)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        TtsHelper.shutdown()
        isShowing = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
