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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DroneViewModel : ViewModel() {

    // ── Existing Firebase state (unchanged) ──────────────────────────────────

    private val _selectedDrone = mutableStateOf<Drone?>(null)
    val selectedDrone: State<Drone?> = _selectedDrone

    private val _drones = mutableStateOf<List<Drone>>(emptyList())
    val drones: State<List<Drone>> = _drones

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()
    private var realtimeListener: ListenerRegistration? = null
    private var fleetListener: ListenerRegistration? = null

    // ── WebSocket additions ──────────────────────────────────────────────────

    /**
     * Dynamic WebSocket configuration for Odisha drone telemetry.
     */
    val webSocketUrl = androidx.compose.runtime.mutableStateOf("ws://10.86.114.194:8765")
    private var _webSocketClient = DroneWebSocketClient(webSocketUrl.value)
    val webSocketClient: DroneWebSocketClient get() = _webSocketClient

    private val _wsConnectionState = MutableStateFlow(false)
    val wsConnectionState: StateFlow<Boolean> = _wsConnectionState

    private val _wsLocation = MutableStateFlow<DroneLocationUpdate?>(null)
    val wsLocation: StateFlow<DroneLocationUpdate?> = _wsLocation

    /**
     * Starts the WebSocket connection and collects incoming drone telemetry.
     * Updates [_selectedDrone] so all existing UI (MapScreen, DroneDetail) works
     * automatically without any other changes.
     *
     * Firebase remains active as a fallback — WebSocket data takes priority
     * because it arrives faster.
     */
    fun startWebSocketUpdates() {
        // Connect on IO thread — avoids blocking the UI/Main thread
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _webSocketClient.connect()
            } catch (e: Exception) {
                Log.e("DroneViewModel", "WebSocket connect failed: ${e.message}")
            }
        }

        // Collect connection state on Main thread
        viewModelScope.launch(Dispatchers.Main) {
            _webSocketClient.connectionState.collect { connected ->
                _wsConnectionState.value = connected
            }
        }

        // Collect updates on Main thread to safely update Compose state
        viewModelScope.launch(Dispatchers.Main) {
            try {
                _webSocketClient.locationFlow.collect { update ->
                    update ?: return@collect   // skip if null

                    _wsLocation.value = update

                    // Merge WebSocket GPS into the existing Drone state so that
                    // MapScreen's liveDronePoint and DroneDetail both auto-update.
                    _selectedDrone.value = _selectedDrone.value?.copy(
                        latitude       = update.latitude,
                        longitude      = update.longitude,
                        battery        = update.battery,
                        altitude       = update.altitude,
                        peopleCount    = update.peopleCount,
                        status         = update.status,
                        payloadDropped = update.payloadDropped
                    ) ?: Drone(
                        // If Firebase hasn't loaded a drone yet, build one from WS data
                        latitude       = update.latitude,
                        longitude      = update.longitude,
                        battery        = update.battery,
                        altitude       = update.altitude,
                        peopleCount    = update.peopleCount,
                        status         = update.status,
                        payloadDropped = update.payloadDropped,
                        droneId        = "drone_1"
                    )

                    Log.d("DroneViewModel", "WS update → ${update.latitude}, ${update.longitude}")
                }
            } catch (e: Exception) {
                Log.e("DroneViewModel", "WebSocket collect error: ${e.message}")
            }
        }
    }

    /** Send target coordinates to the drone server via WebSocket */
    fun sendTargetToDrone(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            _webSocketClient.sendTarget(lat, lng)
        }
    }

    /** Update WebSocket Server URL dynamically and reconnect */
    fun updateWebSocketUrl(newUrl: String) {
        if (newUrl.isBlank() || newUrl == webSocketUrl.value) return
        webSocketUrl.value = newUrl
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _webSocketClient.disconnect()
                _webSocketClient = DroneWebSocketClient(newUrl)
                startWebSocketUpdates()
            } catch (e: Exception) {
                Log.e("DroneViewModel", "Failed to update WebSocket URL: ${e.message}")
            }
        }
    }

    /** Stops the WebSocket connection. */
    fun stopWebSocketUpdates() {
        _webSocketClient.disconnect()
    }


    // ── Existing Firebase methods (completely unchanged) ─────────────────────

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
                    _selectedDrone.value = null
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

    fun startFleetUpdates() {
        fleetListener?.remove()
        _isLoading.value = true
        _errorMessage.value = null

        fleetListener = firestore.collection("drones")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Fleet realtime error: ${error.message}"
                    Log.e("DroneViewModel", "Fleet listener error", error)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                _drones.value = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(Drone::class.java)?.copy(droneId = document.id)
                }
                _isLoading.value = false
            }
    }

    fun stopFleetUpdates() {
        fleetListener?.remove()
        fleetListener = null
    }

    fun clearSelectedDrone() {
        _selectedDrone.value = null
        _errorMessage.value = null
        stopRealtimeUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
        stopFleetUpdates()
        _webSocketClient.disconnect() // clean up WebSocket on ViewModel destroy
    }
}
