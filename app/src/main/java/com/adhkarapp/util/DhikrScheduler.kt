package com.adhkarapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.adhkarapp.model.DhikrAlarm
import com.adhkarapp.receiver.DhikrReceiver
import java.util.Calendar

object DhikrScheduler {

    fun schedule(context: Context, alarm: DhikrAlarm) {
        if (!alarm.isEnabled) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DhikrReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id.hashCode(), intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var nextTime = nextMillis(alarm)
        val now = System.currentTimeMillis()
        if (nextTime <= now) nextTime = now + 60_000L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(nextTime, pendingIntent),
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent)
        }
    }

    fun cancel(context: Context, alarm: DhikrAlarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DhikrReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun nextMillis(alarm: DhikrAlarm): Long {
        val now = Calendar.getInstance()
        val nowMs = now.timeInMillis
        val intervalMs = alarm.intervalMinutes * 60 * 1000L

        fun makeCal(h: Int, m: Int) = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        fun nextRepeatDay(after: Calendar): Long {
            val cal = after.clone() as Calendar
            for (i in 1..7) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                if (alarm.repeatDays.contains(cal.get(Calendar.DAY_OF_WEEK))) {
                    return makeCal(alarm.startHour, alarm.startMinute).apply {
                        set(Calendar.YEAR, cal.get(Calendar.YEAR))
                        set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR))
                    }.timeInMillis
                }
            }
            return makeCal(alarm.startHour, alarm.startMinute).apply {
                add(Calendar.DAY_OF_YEAR, 8)
            }.timeInMillis
        }

        if (alarm.repeatDays.isEmpty()) {
            val single = makeCal(alarm.startHour, alarm.startMinute)
            if (single.timeInMillis <= nowMs) single.add(Calendar.DAY_OF_YEAR, 1)
            return single.timeInMillis
        }

        val today = now.clone() as Calendar
        val todayMs = today.timeInMillis
        val sToday = makeCal(alarm.startHour, alarm.startMinute).apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.DAY_OF_YEAR, today.get(Calendar.DAY_OF_YEAR))
        }
        val eToday = makeCal(alarm.endHour, alarm.endMinute).apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.DAY_OF_YEAR, today.get(Calendar.DAY_OF_YEAR))
        }
        if (eToday.before(sToday)) eToday.add(Calendar.DAY_OF_YEAR, 1)

        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek in alarm.repeatDays) {
            if (todayMs < sToday.timeInMillis) return sToday.timeInMillis
            if (todayMs in sToday.timeInMillis..eToday.timeInMillis) {
                val elapsed = todayMs - sToday.timeInMillis
                val intervals = elapsed / intervalMs
                val nextInWindow = sToday.timeInMillis + (intervals + 1) * intervalMs
                if (nextInWindow <= eToday.timeInMillis) return nextInWindow
            }
        }
        return nextRepeatDay(today)
    }
}
