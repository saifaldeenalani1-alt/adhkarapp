package com.adhkarapp.model

data class TasbihState(
    val id: String = "default",
    val count: Int = 0,
    val target: Int = 33,
    val label: String = "سبحان الله",
    val lastUpdated: Long = 0,
    val soundAtTarget: Boolean = true,
    val vibrateAtTarget: Boolean = true,
    val targetText: String = "الله أكبر"
)
