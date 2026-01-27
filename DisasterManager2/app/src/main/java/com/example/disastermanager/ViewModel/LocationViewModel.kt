package com.example.disastermanager.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {

    private val db = Firebase.firestore

    // Live state for UI
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude

    init {
        fetchDroneLocation()   // start listening immediately
    }


    fun updateDroneLocation(latitude: Double, longitude: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val data = mapOf(
                "latitude" to latitude,
                "longitude" to longitude
            )

            db.collection("location")
                .document("drone_1")
                .set(data)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }
    }


    private fun fetchDroneLocation() {
        db.collection("location")
            .document("drone_1")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _latitude.value = snapshot.getDouble("latitude")
                    _longitude.value = snapshot.getDouble("longitude")
                }
            }
    }
}
