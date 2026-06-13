package com.adhkarapp.data

import android.content.Context
import android.content.SharedPreferences
import com.adhkarapp.model.DhikrAlarm
import com.adhkarapp.model.DhikrItem
import com.adhkarapp.model.TasbihState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("adhkar_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private fun <T> getObject(key: String, type: java.lang.reflect.Type, default: T): T {
        val json = prefs.getString(key, null) ?: return default
        return try { gson.fromJson(json, type) } catch (_: Exception) { default }
    }

    private fun <T> saveObject(key: String, obj: T) {
        prefs.edit().putString(key, gson.toJson(obj)).apply()
    }

    fun getDhikrAlarms(): MutableList<DhikrAlarm> {
        val type = object : TypeToken<MutableList<DhikrAlarm>>() {}.type
        return getObject("dhikr_alarms", type, mutableListOf())
    }

    fun saveDhikrAlarm(alarm: DhikrAlarm) {
        val alarms = getDhikrAlarms()
        val idx = alarms.indexOfFirst { it.id == alarm.id }
        if (idx >= 0) alarms[idx] = alarm else alarms.add(alarm)
        saveObject("dhikr_alarms", alarms)
    }

    fun deleteDhikrAlarm(id: String) {
        val alarms = getDhikrAlarms()
        alarms.removeAll { it.id == id }
        saveObject("dhikr_alarms", alarms)
    }

    fun getTasbihState(): TasbihState {
        val type = object : TypeToken<TasbihState>() {}.type
        return getObject("tasbih_state", type, TasbihState())
    }

    fun saveTasbihState(state: TasbihState) {
        saveObject("tasbih_state", state)
    }

    fun getCustomDhikrItems(): MutableList<DhikrItem> {
        val type = object : TypeToken<MutableList<DhikrItem>>() {}.type
        return getObject("custom_dhikr", type, mutableListOf())
    }

    fun saveCustomDhikrItem(item: DhikrItem) {
        val items = getCustomDhikrItems()
        val idx = items.indexOfFirst { it.id == item.id }
        if (idx >= 0) items[idx] = item else items.add(item)
        saveObject("custom_dhikr", items)
    }

    fun deleteCustomDhikrItem(id: String) {
        val items = getCustomDhikrItems()
        items.removeAll { it.id == id }
        saveObject("custom_dhikr", items)
    }
}
