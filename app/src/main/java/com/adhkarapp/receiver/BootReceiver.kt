package com.adhkarapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.adhkarapp.data.PreferencesManager
import com.adhkarapp.util.DhikrScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Adhkar:BootReceiver")
        wakeLock.acquire(10_000L)

        try {
            val prefs = PreferencesManager(context)
            prefs.getDhikrAlarms().forEach { alarm ->
                if (alarm.isEnabled) DhikrScheduler.schedule(context, alarm)
            }
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }
}
