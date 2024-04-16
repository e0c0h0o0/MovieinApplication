package com.cs501finalproj.justmovein

data class Housing(
    var title: String? = null,
    var description: String? = null,
    var beds: Int? = null,
    var bath: Int? = null,
    var location: String? = null,
    var price: Double? = null
)