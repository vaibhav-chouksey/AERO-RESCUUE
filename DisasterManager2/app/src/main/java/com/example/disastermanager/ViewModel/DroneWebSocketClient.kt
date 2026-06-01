package com.example.disastermanager.ViewModel

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Data class representing a real-time drone location update received via WebSocket.
 */
data class DroneLocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val battery: Int = 0,
    val altitude: Double = 0.0,
    val peopleCount: Int = 0,
    val status: String = "active",
    val payloadDropped: Boolean = false
)

/**
 * WebSocket client that connects to a drone telemetry server and streams
 * real-time GPS + sensor data to the app.
 *
 * Usage:
 *   val client = DroneWebSocketClient("ws://192.168.1.100:8765")
 *   client.connect()
 *   // observe client.locationFlow in a ViewModel
 *   client.disconnect() // call in onCleared()
 */
class DroneWebSocketClient(private val serverUrl: String) {

    // OkHttp client with sensible timeouts for drone telemetry
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS) // keep-alive pings
        .build()

    private var webSocket: WebSocket? = null
    private var isIntentionallyClosed = false

    // ── StateFlows observed by DroneViewModel ────────────────────────────────

    /** Emits each incoming drone location/telemetry update */
    private val _locationFlow = MutableStateFlow<DroneLocationUpdate?>(null)
    val locationFlow: StateFlow<DroneLocationUpdate?> = _locationFlow

    /** true = WebSocket connected, false = disconnected/reconnecting */
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    // ── Public API ───────────────────────────────────────────────────────────

    /** Open the WebSocket connection to the drone telemetry server. */
    fun connect() {
        isIntentionallyClosed = false
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = okHttpClient.newWebSocket(request, DroneWebSocketListener())
        Log.d(TAG, "Connecting to $serverUrl …")
    }

    /** Close the connection cleanly. Call this from ViewModel.onCleared(). */
    fun disconnect() {
        isIntentionallyClosed = true
        webSocket?.close(1000, "App closed")
        webSocket = null
        _connectionState.value = false
        Log.d(TAG, "WebSocket disconnected intentionally.")
    }

    /** Send target coordinates to the drone server so it simulates routing dynamically */
    fun sendTarget(lat: Double, lng: Double) {
        try {
            val json = org.json.JSONObject().apply {
                put("type", "set_target")
                put("lat", lat)
                put("lng", lng)
            }
            webSocket?.send(json.toString())
            Log.d(TAG, "Sent target coordinates to server: $lat, $lng")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send target coordinates to server: ${e.message}")
        }
    }

    // ── Private listener ─────────────────────────────────────────────────────

    private inner class DroneWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionState.value = true
            Log.d(TAG, "✅ WebSocket connected to drone server.")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val json = JSONObject(text)

                val update = DroneLocationUpdate(
                    latitude     = json.getDouble("lat"),
                    longitude    = json.getDouble("lng"),
                    battery      = json.optInt("battery", 0),
                    altitude     = json.optDouble("altitude", 0.0),
                    peopleCount  = json.optInt("people_count", 0),
                    status       = json.optString("status", "active"),
                    payloadDropped = json.optBoolean("payload_dropped", false)
                )

                _locationFlow.value = update
                Log.v(TAG, "📍 Drone @ ${update.latitude}, ${update.longitude}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to parse WebSocket message: $text — ${e.message}")
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _connectionState.value = false
            Log.e(TAG, "❌ WebSocket failure: ${t.message}")

            // Auto-reconnect after 3 seconds (only if not intentionally closed)
            if (!isIntentionallyClosed) {
                Log.d(TAG, "🔄 Reconnecting in 3 seconds …")
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (!isIntentionallyClosed) connect()
                }, RECONNECT_DELAY_MS)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionState.value = false
            Log.d(TAG, "🔌 WebSocket closed — code=$code reason=$reason")
        }
    }

    companion object {
        private const val TAG = "DroneWebSocket"
        private const val RECONNECT_DELAY_MS = 3000L
    }
}
