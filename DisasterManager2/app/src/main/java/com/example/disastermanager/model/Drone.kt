package com.example.disastermanager.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class Drone(
    // ---------------- EXISTING FIELDS (unchanged) ----------------
    var battery: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var payload: String = "",          // e.g. "0 People Detected"
    var peopleCount: Int = 0,
    var droneId: String = "",
    var status: String = "idle",
    var timestamp: Timestamp? = null,
    var imageUrl: String = "",
    var altitude: Double = 0.0,

    // ---------------- NEW FIELDS YOU ASKED TO ADD ----------------
    var filename: String = "",
    var flightMode: String = "",
    var isArmed: Boolean = false,
    var payloadDropped: Boolean = false,
    var voltage: Double = 0.0,
    var videoFile: String = "",
    var timestamp_str: String = "",
    var lastActive: Timestamp? = null,
    var lastTelemetryUpdate: Timestamp? = null,
    var lastUploadTime: Timestamp? = null
) {

    // ---------------- EXISTING FUNCTIONS (kept exactly) ----------------
    fun getFormattedTimestamp(): String {
        if (timestamp == null) return "N/A"

        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(timestamp!!.toDate())
    }




    fun getLocationString(): String {
        return String.format("%.4f, %.4f", latitude, longitude)
    }
}
