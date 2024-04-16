package com.cs501finalproj.justmovein

data class Item(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val condition: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    val isActive: Boolean? = true,
    val likedBy: Map<String, Boolean>? = null,
    val timestamp: Long? = null
)