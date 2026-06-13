package com.adhkarapp

import android.app.Application
import com.adhkarapp.data.PreferencesManager
import com.adhkarapp.util.DhikrScheduler

class AdhkarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = PreferencesManager(this)
        prefs.getDhikrAlarms().forEach { alarm ->
            if (alarm.isEnabled) DhikrScheduler.schedule(this, alarm)
        }
    }
}
