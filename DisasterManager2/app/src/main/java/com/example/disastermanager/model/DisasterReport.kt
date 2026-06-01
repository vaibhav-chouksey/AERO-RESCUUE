package com.example.disastermanager.model

data class DisasterReport(
    val id: String = "",
    val reportNo: String = "",
    val timestamp: Long = 0L,
    val title: String = "",
    val description: String = "",
    val disasterType: String = "Flood",
    val emergencyLevel: String = "Medium",
    val status: String = "Pending",
    val imageBase64: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val userId: String = "",
    val userName: String = ""
)
