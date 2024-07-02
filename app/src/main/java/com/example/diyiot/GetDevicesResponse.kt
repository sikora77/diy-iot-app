package com.example.diyiot

data class GetDevicesResponse(
    val lights: List<Light>,
    val status: Int
)