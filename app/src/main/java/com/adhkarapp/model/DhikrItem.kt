package com.adhkarapp.model

data class DhikrItem(
    val id: String,
    val text: String,
    val category: String,
    val source: String,
    val count: Int = 1
)
