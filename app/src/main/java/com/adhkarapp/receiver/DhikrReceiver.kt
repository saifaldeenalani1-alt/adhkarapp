package com.adhkarapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.adhkarapp.data.DhikrData
import com.adhkarapp.data.PreferencesManager
import com.adhkarapp.service.DhikrOverlayService
import com.adhkarapp.util.DhikrScheduler
import java.util.Calendar

class DhikrReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val prefs = PreferencesManager(context)
        val alarms = prefs.getDhikrAlarms()
        val alarm = alarms.find { it.id == alarmId } ?: return
        if (!alarm.isEnabled) return

        if (alarm.repeatDays.isNotEmpty()) {
            val now = Calendar.getInstance()
            val today = now.get(Calendar.DAY_OF_WEEK)
            if (today !in alarm.repeatDays) {
                DhikrScheduler.schedule(context, alarm)
                return
            }
            val endCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.endHour)
                set(Calendar.MINUTE, alarm.endMinute)
                set(Calendar.SECOND, 0)
            }
            if (now.after(endCal)) {
                DhikrScheduler.schedule(context, alarm)
                return
            }
        }

        val selected = DhikrData.getByIds(alarm.dhikrIds)
        val dhikr = if (selected.isNotEmpty()) selected.random() else null
        val text = dhikr?.text ?: "سبحان الله وبحمده"

        val overlayIntent = Intent(context, DhikrOverlayService::class.java).apply {
            putExtra("dhikr_text", text)
            putExtra("dhikr_source", dhikr?.source ?: "")
            putExtra("display_seconds", alarm.displayDurationSeconds)
            putExtra("play_audio", alarm.playAudio)
            putExtra("audio_repeat", alarm.audioRepeatCount)
        }
        try {
            context.startForegroundService(overlayIntent)
        } catch (_: Exception) {
            context.startService(overlayIntent)
        }

        if (alarm.repeatDays.isNotEmpty()) {
            DhikrScheduler.schedule(context, alarm)
        }
    }
}
