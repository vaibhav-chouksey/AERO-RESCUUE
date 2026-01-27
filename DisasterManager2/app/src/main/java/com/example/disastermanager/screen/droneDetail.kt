package com.example.disastermanager.screen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.disastermanager.ViewModel.DroneViewModel
import com.example.disastermanager.ViewModel.ImageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroneDetailScreen(droneId: String) {

    val droneViewModel: DroneViewModel = viewModel()
    val imageViewModel: ImageViewModel = viewModel() // 1. Initialize ImageViewModel

    val drone = droneViewModel.selectedDrone.value
    val isLoading = droneViewModel.isLoading.value
    val errorMessage = droneViewModel.errorMessage.value

    // Image states
    val disasterImage = imageViewModel.disasterImage.value

    LaunchedEffect(droneId) {
        droneViewModel.startRealtimeUpdates(droneId)
        // ImageViewModel starts its updates in its init block
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drone Details: $droneId") },
                navigationIcon = {
                    IconButton(onClick = { /* Assuming a method to navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                drone != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // 🔵 STATUS CARD
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Status", style = MaterialTheme.typography.titleMedium)
                                Text(
//                                    text = drone.status.replaceFirstChar { it.uppercase() },
                                    text = "Assigned",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // 🖼 LIVE DISASTER IMAGE (NEW SECTION)
                        if (imageViewModel.isLoading.value) {
                            // Display a placeholder while loading the image URL
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else if (disasterImage?.url != null && disasterImage.url.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Live Aerial View",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    AsyncImage(
                                        model = disasterImage.url, // URL from Firebase
                                        contentDescription = "Live Aerial View from Drone",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp) // Fixed height for image display
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Text(
                                        text = "Last captured: ${disasterImage.timestamp}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        } else {
                            // Optional: Show message if no image URL is found
                            Text("No live image available.", color = Color.Gray)
                        }


                        // 🔋 BATTERY
                        DetailCard(
                            icon = Icons.Default.BatteryFull,
                            title = "Battery",
                            value = "${drone.voltage}V",
//                            color = if (drone.voltage > 20) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )

                        // 📍 LOCATION
                        DetailCard(
                            icon = Icons.Default.LocationOn,
                            title = "Location",
                            value = drone.getLocationString()
                        )

                        // 👥 PEOPLE DETECTED (STRING)
                        DetailCard(
                            icon = Icons.Default.PersonSearch,
                            title = "Detection",
                            value = drone.payload
                        )

                        // 🔢 PEOPLE COUNT (Assuming `peopleCount` exists in your Drone data model)
                        // If it doesn't exist, you'll need to define it or fetch it.
                        DetailCard(
                            icon = Icons.Default.Tag,
                            title = "People Count",
                            value = drone.peopleCount.toString()
                        )

                        // ⏱ LAST UPDATE
                        // 🟢 LAST ACTIVE (Realtime)
                        DetailCard(
                            icon = Icons.Default.AccessTime,
                            title = "Last Active",
                            value = drone.lastActive?.toDate()?.let {
                                java.text.SimpleDateFormat(
                                    "dd MMM yyyy, hh:mm:ss a",
                                    java.util.Locale.getDefault()
                                ).format(it)
                            } ?: "N/A"
                        )

                        // 🛫 ALTITUDE
                        DetailCard(
                            icon = Icons.Default.Height,
                            title = "Altitude",
                            value = "${drone.altitude} m"
                        )


                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
