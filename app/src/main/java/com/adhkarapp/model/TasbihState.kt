package com.adhkarapp.model

import com.adhkarapp.model.DhikrItem

data class TasbihState(
    val id: String = "default",
    val count: Int = 0,
    val target: Int = 33,
    val label: String = "سبحان الله",
    val lastUpdated: Long = 0,
    val soundAtTarget: Boolean = true,
    val vibrateAtTarget: Boolean = true,
    val targetText: String = "الله أكبر",
    val dhikrItem: DhikrItem? = null,
    val ttsEnabled: Boolean = false
)
