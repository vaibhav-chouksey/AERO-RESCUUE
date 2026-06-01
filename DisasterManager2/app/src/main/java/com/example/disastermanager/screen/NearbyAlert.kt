package com.example.disastermanager.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.disastermanager.ViewModel.DroneViewModel
import com.example.disastermanager.model.Drone
import com.example.disastermanager.pages.WeatherPage.LocationAccess.LocationUtils
import com.example.disastermanager.pages.WeatherPage.LocationAccess.locationViewmodel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private data class ActiveEmergency(
    val id: String,
    val type: String,
    val status: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyAlertsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val context = LocalContext.current
    val locationViewModel: locationViewmodel = viewModel()
    val droneViewModel: DroneViewModel = viewModel()
    val locationUtils = remember { LocationUtils(context) }
    val emergencies = remember { mutableStateListOf<ActiveEmergency>() }
    val userLocation = locationViewModel.location.value
    val drones by droneViewModel.drones

    LaunchedEffect(Unit) {
        if (locationUtils.hasLocationPermission(context)) {
            locationUtils.requestLocationUpdates(locationViewModel)
        }
        droneViewModel.startFleetUpdates()
    }

    DisposableEffect(Unit) {
        val registration = Firebase.firestore.collection("emergency")
            .addSnapshotListener { snapshot, _ ->
                emergencies.clear()
                snapshot?.documents?.mapNotNullTo(emergencies) { document ->
                    val status = document.getString("status") ?: "Registered"
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")

                    if (latitude == null || longitude == null || status == "Resolved") {
                        null
                    } else {
                        ActiveEmergency(
                            id = document.id,
                            type = document.getString("emergencyType") ?: "Emergency",
                            status = status,
                            latitude = latitude,
                            longitude = longitude
                        )
                    }
                }
            }
        onDispose { registration.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Alerts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LocationSummaryCard(
                    locationText = userLocation?.let { "%.5f, %.5f".format(it.latitude, it.longitude) }
                        ?: "Waiting for live GPS"
                )
            }

            item {
                Text(
                    "Active Emergencies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            items(emergencies.sortedBy {
                userLocation?.let { location ->
                    distanceKm(location.latitude, location.longitude, it.latitude, it.longitude)
                } ?: Double.MAX_VALUE
            }) { emergency ->
                val distance = userLocation?.let {
                    distanceKm(it.latitude, it.longitude, emergency.latitude, emergency.longitude)
                }
                NearbyInfoCard(
                    icon = Icons.Default.Warning,
                    title = emergency.type,
                    subtitle = emergency.status,
                    distanceText = distance?.let { "%.2f km away".format(it) } ?: "Distance unavailable",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Active Drones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            items(drones.sortedBy {
                userLocation?.let { location ->
                    distanceKm(location.latitude, location.longitude, it.latitude, it.longitude)
                } ?: Double.MAX_VALUE
            }) { drone ->
                val distance = userLocation?.let {
                    distanceKm(it.latitude, it.longitude, drone.latitude, drone.longitude)
                }
                DroneNearbyCard(drone, distance)
            }
        }
    }
}

@Composable
private fun LocationSummaryCard(locationText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Your Live Location", color = Color(0xFFBDBDBD), style = MaterialTheme.typography.labelMedium)
            Text(locationText, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun DroneNearbyCard(drone: Drone, distance: Double?) {
    NearbyInfoCard(
        icon = Icons.Default.Flight,
        title = drone.droneId.ifBlank { "Drone" },
        subtitle = "${drone.status} • ${drone.battery}% battery",
        distanceText = distance?.let { "%.2f km away".format(it) } ?: "Distance unavailable",
        tint = Color(0xFF64B5F6)
    )
}

@Composable
private fun NearbyInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    distanceText: String,
    tint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(color = tint.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color(0xFFBDBDBD), style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(distanceText, color = tint, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val radiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2).pow(2.0)
    return radiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
}
