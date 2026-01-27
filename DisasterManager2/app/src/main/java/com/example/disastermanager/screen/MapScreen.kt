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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disastermanager.R
import com.example.disastermanager.ViewModel.DroneViewModel
import com.example.disastermanager.ViewModel.LocationViewModel
import com.example.disastermanager.pages.WeatherPage.LocationAccess.locationViewmodel
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

    // 🔥 DRONE (LIVE LOCATION FROM Firebase → drones/drone_1)
    val droneVM: DroneViewModel = viewModel()
    val droneData = droneVM.selectedDrone.value

    LaunchedEffect(Unit) {
        droneVM.startRealtimeUpdates("drone_1")
    }

    // 🔥 TARGET (MISSION LOCATION FROM Firebase → location/drone_1)
    val targetVM: LocationViewModel = viewModel()
    val targetLat = targetVM.latitude.collectAsState()
    val targetLng = targetVM.longitude.collectAsState()

    val destinationPoint =
        if (targetLat.value != null && targetLng.value != null)
            Point.fromLngLat(targetLng.value!!, targetLat.value!!)
        else
            Point.fromLngLat(83.814220, 19.074140)  // fallback


    // 🔥 USER HELP CENTER (Mobile GPS → locationViewmodel from WeatherPage)
    val userVM: locationViewmodel = viewModel()
    val userLocation = userVM.location.value

    val userPoint =
        if (userLocation != null)
            Point.fromLngLat(userLocation.longitude, userLocation.latitude)
        else null


    // DRONE POINT
    val liveDronePoint =
        if (droneData != null)
            Point.fromLngLat(droneData.longitude, droneData.latitude)
        else null


    // MAP CAMERA
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(13.0)
            center(destinationPoint)
        }
    }

    // DISTANCE
    val distanceText = if (liveDronePoint != null) {
        val km = TurfMeasurement.distance(liveDronePoint, destinationPoint)
        String.format("Drone is %.2f km away", km)
    } else "Locating Drone..."


    // ============================
    // UI START
    // ============================
    Box(modifier = modifier.fillMaxSize()) {

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState
        ) {

            // ⭐ TARGET MARKER
            val targetIcon = rememberIconImage(R.drawable.marker4)
            PointAnnotation(point = destinationPoint) {
                iconImage = targetIcon
                iconSize = 0.3
                textField = "DESTINATION"
                textSize = 14.0
                textColor = Color.Red
                textOffset = listOf(0.0, -2.0)
            }

            // ⭐ USER MARKER (Help Center)
            if (userPoint != null) {
                val userMarker = rememberIconImage(R.drawable.marker3)
                PointAnnotation(point = userPoint) {
                    iconImage = userMarker
                    iconSize = 0.2
                    textField = "HELP CENTER"
                    textSize = 14.0
                    textOffset = listOf(0.0, -2.0)
                }
            }

            // ⭐ DRONE MARKER (Live)
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

                PolylineAnnotation(points = listOf(liveDronePoint, destinationPoint)) {
                    lineColor = Color(0xFF2196F3)
                    lineWidth = 3.0
                }
            }
        }

        // ⭐ INFO CARD
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Live Mission Status", color = Color.Gray)
                Text(
                    text = distanceText,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ⭐ RE-CENTER BUTTON
        FloatingActionButton(
            onClick = {
                if (liveDronePoint != null) {
                    viewportState.flyTo(
                        CameraOptions.Builder()
                            .center(liveDronePoint)
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
