package com.example.diyiot

data class Device(
    val brightness: Int,
    val id: String,
    val is_on: Boolean,
    val name: String,
    val nicknames: List<Any>,
    val rgb: Int,
    val type_: String
)