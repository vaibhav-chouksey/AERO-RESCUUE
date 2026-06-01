package com.example.disastermanager.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disastermanager.R
import com.example.disastermanager.ViewModel.DroneViewModel
import com.example.disastermanager.ViewModel.LocationViewModel
import com.example.disastermanager.model.DisasterReport
import com.example.disastermanager.pages.WeatherPage.LocationAccess.locationViewmodel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.turf.TurfMeasurement

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val droneBasePoint = Point.fromLngLat(76.22611909999999, 21.3122487)
    val activeReports = remember { mutableStateListOf<DisasterReport>() }

    // 🔥 DRONE (LIVE LOCATION FROM Firebase → drones/drone_1)
    val droneVM: DroneViewModel = viewModel()
    val droneData = droneVM.selectedDrone.value
    val drones by droneVM.drones

    // 📡 WebSocket connection state — initial=false prevents null crash on first frame
    val wsConnected by droneVM.wsConnectionState.collectAsState(initial = false)

    // Start Firebase listener & WebSocket URL initialization
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("telemetry_prefs", android.content.Context.MODE_PRIVATE)
        val savedUrl = sharedPref.getString("ws_url", "ws://10.86.114.194:8765") ?: "ws://10.86.114.194:8765"
        droneVM.updateWebSocketUrl(savedUrl)

        droneVM.startRealtimeUpdates("drone_1")
        droneVM.startFleetUpdates()
    }

    // Start WebSocket separately — avoids blocking Firebase setup
    LaunchedEffect("websocket") {
        droneVM.startWebSocketUpdates()
    }

    // 🔥 TARGET (MISSION LOCATION FROM Firebase → location/drone_1)
    val targetVM: LocationViewModel = viewModel()
    val targetLat = targetVM.latitude.collectAsState()
    val targetLng = targetVM.longitude.collectAsState()

    val destinationPoint =
        if (targetLat.value != null && targetLng.value != null)
            Point.fromLngLat(targetLng.value!!, targetLat.value!!)
        else
            Point.fromLngLat(76.2350, 21.3200)  // fallback (near home)

    // Sync destination with WebSocket drone server automatically
    LaunchedEffect(destinationPoint, wsConnected) {
        if (wsConnected) {
            droneVM.sendTargetToDrone(destinationPoint.latitude(), destinationPoint.longitude())
        }
    }

    // 🔥 USER HELP CENTER (Mobile GPS → locationViewmodel from WeatherPage)
    val userVM: locationViewmodel = viewModel()
    val userLocation = userVM.location.value

    val userPoint =
        if (userLocation != null)
            Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        else null

    DisposableEffect(Unit) {
        val registration = Firebase.firestore.collection("disaster_reports")
            .addSnapshotListener { snapshot, _ ->
                activeReports.clear()
                snapshot?.documents?.forEach { document ->
                    document.toObject(DisasterReport::class.java)?.let { report ->
                        if (report.status != "Resolved") {
                            activeReports.add(report.copy(id = document.id))
                        }
                    }
                }
            }

        onDispose { registration.remove() }
    }

    // DRONE POINT
    val liveDronePoint =
        if (droneData != null)
            Point.fromLngLat(droneData.longitude, droneData.latitude)
        else null

    // MAP CAMERA
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(13.0)
            center(userPoint ?: droneBasePoint)
        }
    }

    // DISTANCE
    val totalRouteKm = TurfMeasurement.distance(droneBasePoint, destinationPoint)
    val remainingKm = if (liveDronePoint != null) TurfMeasurement.distance(liveDronePoint, destinationPoint) else null
    val distanceText = if (remainingKm != null) {
        String.format("Route: %.2f km | Drone is %.2f km away", totalRouteKm, remainingKm)
    } else {
        String.format("Route: %.2f km | Locating Drone...", totalRouteKm)
    }


    // ============================
    // UI START
    // ============================
    Box(modifier = modifier.fillMaxSize()) {

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState
        ) {

            // ⭐ TARGET MARKER
            val baseIcon = rememberIconImage(R.drawable.marker4)
            CircleAnnotation(point = droneBasePoint) {
                circleRadius = 54.0
                circleColor = Color(0xFF4CAF50)
                circleOpacity = 0.12
                circleStrokeWidth = 2.0
                circleStrokeColor = Color(0xFF4CAF50)
            }
            PointAnnotation(point = droneBasePoint) {
                iconImage = baseIcon
                iconSize = 0.25
                textField = "DRONE BASE"
                textSize = 13.0
                textColor = Color(0xFF4CAF50)
                textOffset = listOf(0.0, -2.0)
            }

            val targetIcon = rememberIconImage(R.drawable.marker4)
            PointAnnotation(point = destinationPoint) {
                iconImage = targetIcon
                iconSize = 0.3
                textField = "DESTINATION"
                textSize = 14.0
                textColor = Color.Red
                textOffset = listOf(0.0, -2.0)
            }

            // ⭐ PLANNED ROUTE (Base to Destination)
            PolylineAnnotation(points = listOf(droneBasePoint, destinationPoint)) {
                lineColor = Color(0xFF9E9E9E)
                lineWidth = 2.5
            }

            // ⭐ USER MARKER (Help Center)
            if (userPoint != null) {
                val userMarker = rememberIconImage(R.drawable.marker3)
                PointAnnotation(point = userPoint) {
                    iconImage = userMarker
                    iconSize = 0.2
                    textField = "USER LOCATION"
                    textSize = 14.0
                    textOffset = listOf(0.0, -2.0)
                }
            }

            // ⭐ DRONE MARKER (Live — updates from WebSocket or Firebase)
            activeReports.forEach { report ->
                val reportPoint = Point.fromLngLat(report.longitude, report.latitude)

                CircleAnnotation(point = reportPoint) {
                    circleRadius = 40.0
                    circleColor = Color.Red
                    circleOpacity = 0.10
                    circleStrokeWidth = 2.0
                    circleStrokeColor = Color.Red
                }

                PointAnnotation(point = reportPoint) {
                    textField = report.disasterType.uppercase()
                    textSize = 12.0
                    textColor = Color.Red
                    textOffset = listOf(0.0, -2.0)
                }
            }

            drones.forEach { drone ->
                if (drone.latitude != 0.0 || drone.longitude != 0.0) {
                    val dronePoint = Point.fromLngLat(drone.longitude, drone.latitude)

                    CircleAnnotation(point = dronePoint) {
                        circleRadius = 8.0
                        circleColor = if (drone.status == "Available") Color(0xFF4CAF50) else Color(0xFF2196F3)
                        circleStrokeWidth = 2.0
                        circleStrokeColor = Color.White
                    }

                    PointAnnotation(point = dronePoint) {
                        textField = drone.droneId.ifBlank { "DRONE" }
                        textSize = 11.0
                        textColor = Color(0xFF2196F3)
                        textOffset = listOf(0.0, 2.0)
                    }
                }
            }

            if (liveDronePoint != null) {

                CircleAnnotation(point = liveDronePoint) {
                    circleRadius = 12.0
                    circleColor = Color.White
                    circleOpacity = 0.5
                }

                CircleAnnotation(point = liveDronePoint) {
                    circleRadius = 8.0
                    circleColor = Color(0xFF2196F3)
                    circleStrokeWidth = 2.0
                    circleStrokeColor = Color.White
                }

                PointAnnotation(point = liveDronePoint) {
                    textField = "DRONE 1 (LIVE)"
                    textSize = 12.0
                    textColor = Color(0xFF2196F3)
                    textOffset = listOf(0.0, 2.0)
                }

                // Covered flight path (solid blue, slightly thicker)
                PolylineAnnotation(points = listOf(droneBasePoint, liveDronePoint)) {
                    lineColor = Color(0xFF2196F3)
                    lineWidth = 4.0
                }

                // Remaining flight path (solid blue, slightly thinner)
                PolylineAnnotation(points = listOf(liveDronePoint, destinationPoint)) {
                    lineColor = Color(0xFF2196F3)
                    lineWidth = 3.0
                }
            }
        }

        // ⭐ INFO CARD (with WebSocket live indicator badge)
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Live Mission Status", color = Color.Gray)
                Text(
                    text = distanceText,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // WebSocket / Firebase indicator badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (wsConnected) Color(0xFF1B5E20) else Color(0xFF1A237E)
                ) {
                    Text(
                        text = if (wsConnected) "⚡ WebSocket Live" else "☁ Firebase Fallback",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                MapLegendRow(Color(0xFF4CAF50), "Base route")
                MapLegendRow(Color(0xFF2196F3), "Active drones")
                MapLegendRow(Color.Red, "Disaster zones")
            }
        }

        // ⭐ RE-CENTER BUTTON
        FloatingActionButton(
            onClick = {
                if (userPoint != null) {
                    viewportState.flyTo(
                        CameraOptions.Builder()
                            .center(userPoint)
                            .zoom(16.0)
                            .build()
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "", tint = Color.White)
        }
    }
}

@Composable
private fun MapLegendRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = color,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.size(10.dp)
        ) {}
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
