package com.example.disastermanager.model

data class UserModel(
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    val phone: String = "",
    val address: String = "",
    val gender: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val fcmToken: String = ""
)
