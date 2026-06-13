package com.adhkarapp.model

import java.util.Calendar
import java.util.UUID

data class DhikrAlarm(
    val id: String = UUID.randomUUID().toString(),
    val isEnabled: Boolean = true,
    val label: String = "",
    val intervalMinutes: Int = 30,
    val startHour: Int = 6,
    val startMinute: Int = 0,
    val endHour: Int = 22,
    val endMinute: Int = 0,
    val repeatDays: Set<Int> = (Calendar.SUNDAY..Calendar.SATURDAY).toSet(),
    val dhikrIds: Set<String> = emptySet(),
    val displayDurationSeconds: Int = 10,
    val playAudio: Boolean = false,
    val audioRepeatCount: Int = 1
)
