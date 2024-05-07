package com.cs501finalproj.justmovein

data class Housing(
    var id: String? = null,
    var title: String? = null,
    var description: String? = null,
    val category: String? = null,
    var price: Double? = null,
    val imageUrls: List<String>? = null,
    var active: Boolean? = true,
    var likedBy: MutableMap<String, Boolean>? = mutableMapOf(),
    val timestamp: Long? = null,
    val sellerId: String? = null
)