package com.cs501finalproj.justmovein

data class Item(
    var id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val condition: String? = null,
    val price: Double? = null,
    val imageUrls: List<String>? = null,
    var active: Boolean? = true,
    var likedBy: MutableMap<String, Boolean>? = mutableMapOf(),
    val timestamp: Long? = null,
    val sellerId: String? = null
)