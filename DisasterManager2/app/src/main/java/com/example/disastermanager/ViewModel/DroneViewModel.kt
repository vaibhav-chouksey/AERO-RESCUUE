package com.example.disastermanager.ViewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disastermanager.model.Drone
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DroneViewModel : ViewModel() {

    private val _selectedDrone = mutableStateOf<Drone?>(null)
    val selectedDrone: State<Drone?> = _selectedDrone

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()
    private var realtimeListener: ListenerRegistration? = null

    fun fetchDroneData(droneId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = firestore.collection("drones")
                    .document(droneId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val drone = documentSnapshot.toObject(Drone::class.java)
                    _selectedDrone.value = drone?.copy(droneId = droneId)
                } else {
                    _selectedDrone.value = null                 // CLEAR old value
                    _errorMessage.value = "Drone not found."
                }
            } catch (e: Exception) {
                _selectedDrone.value = null
                _errorMessage.value = "Error fetching data: ${e.message}"
                Log.e("DroneViewModel", "Error fetching drone data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Optional: use realtime snapshot listener if you want live updates.
     * Call startRealtimeUpdates("drone_1") and later stopRealtimeUpdates()
     */
    fun startRealtimeUpdates(droneId: String) {
        stopRealtimeUpdates() // ensure single listener
        _isLoading.value = true
        _errorMessage.value = null

        realtimeListener = firestore.collection("drones")
            .document(droneId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Realtime error: ${error.message}"
                    Log.e("DroneViewModel", "Realtime listener error", error)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val drone = snapshot.toObject(Drone::class.java)
                    _selectedDrone.value = drone?.copy(droneId = droneId)
                } else {
                    _selectedDrone.value = null
                    _errorMessage.value = "Drone not found."
                }
                _isLoading.value = false
            }
    }

    fun stopRealtimeUpdates() {
        realtimeListener?.remove()
        realtimeListener = null
    }

    fun clearSelectedDrone() {
        _selectedDrone.value = null
        _errorMessage.value = null
        stopRealtimeUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
    }

}
