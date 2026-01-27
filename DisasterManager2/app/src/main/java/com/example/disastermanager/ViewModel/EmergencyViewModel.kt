package com.example.disastermanager.ViewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class EmergencyViewModel:ViewModel() {
    fun sendEmergencyAlert(
        context: Context,
        latitude: Double?,
        longitude: Double?
    ) {


        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->

                val name = document.getString("name") ?: "Unknown"
                val phone = document.getString("phone") ?: "Not Available"

                val emergencyData = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "phone" to phone,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("emergency")
                    .add(emergencyData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Emergency alert sent!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to send emergency alert!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "User data fetch failed!", Toast.LENGTH_SHORT).show()
            }
    }

}