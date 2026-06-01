package com.example.disastermanager.ViewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class EmergencyViewModel:ViewModel() {
    fun sendEmergencyAlert(
        context: Context,
        latitude: Double?,
        longitude: Double?,
        emergencyType: String = "SOS",
        onComplete: (Boolean) -> Unit = {}
    ) {


        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Please sign in before sending SOS", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }
        val db = Firebase.firestore

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->

                val name = document.getString("name") ?: "Unknown"
                val phone = document.getString("phone") ?: "Not Available"

                val emergencyData = mapOf(
                    "userId" to uid,
                    "userName" to name,
                    "phone" to phone,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "timestamp" to System.currentTimeMillis(),
                    "emergencyType" to emergencyType,
                    "status" to "Registered"
                )

                db.collection("emergency")
                    .add(emergencyData)
                    .addOnSuccessListener { emergencyDocument ->
                        assignNearestAvailableDrone(
                            incidentId = emergencyDocument.id,
                            incidentLatitude = latitude ?: 0.0,
                            incidentLongitude = longitude ?: 0.0
                        )
                        Toast.makeText(context, "Emergency alert sent!", Toast.LENGTH_SHORT).show()
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to send emergency alert!", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "User data fetch failed!", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
    }

    private fun assignNearestAvailableDrone(
        incidentId: String,
        incidentLatitude: Double,
        incidentLongitude: Double
    ) {
        val db = Firebase.firestore

        db.collection("drones")
            .whereEqualTo("status", "Available")
            .get()
            .addOnSuccessListener { snapshot ->
                val nearest = snapshot.documents.minByOrNull { document ->
                    val droneLatitude = document.getDouble("latitude") ?: 0.0
                    val droneLongitude = document.getDouble("longitude") ?: 0.0
                    distanceKm(incidentLatitude, incidentLongitude, droneLatitude, droneLongitude)
                } ?: return@addOnSuccessListener

                nearest.reference.update(
                    mapOf(
                        "status" to "Assigned",
                        "assignedIncidentId" to incidentId
                    )
                )
            }
    }

    private fun distanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val radiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radiusKm * c
    }
}
