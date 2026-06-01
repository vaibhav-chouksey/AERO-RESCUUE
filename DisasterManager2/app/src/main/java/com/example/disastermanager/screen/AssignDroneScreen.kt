package com.example.disastermanager.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disastermanager.ViewModel.DroneViewModel
import com.example.disastermanager.ViewModel.LocationViewModel
import com.example.disastermanager.pages.WeatherPage.LocationAccess.LocationUtils
import com.example.disastermanager.pages.WeatherPage.LocationAccess.locationViewmodel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignDroneScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val droneViewModel: DroneViewModel = viewModel()
    val locationVM: locationViewmodel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()
    val locationUtils = remember { LocationUtils(context) }

    val droneList by droneViewModel.drones
    val userLocation = locationVM.location.value

    var selectedDroneId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var targetLat by remember { mutableStateOf("") }
    var targetLng by remember { mutableStateOf("") }
    var isAssigning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        droneViewModel.startFleetUpdates()
        if (locationUtils.hasLocationPermission(context)) {
            locationUtils.requestLocationUpdates(locationVM)
        }
        val sharedPref = context.getSharedPreferences("telemetry_prefs", android.content.Context.MODE_PRIVATE)
        val savedUrl = sharedPref.getString("ws_url", "ws://10.86.114.194:8765") ?: "ws://10.86.114.194:8765"
        droneViewModel.updateWebSocketUrl(savedUrl)
    }

    // Auto-fill with user's GPS location
    LaunchedEffect(userLocation) {
        if (userLocation != null && targetLat.isEmpty() && targetLng.isEmpty()) {
            targetLat = "%.6f".format(userLocation.latitude)
            targetLng = "%.6f".format(userLocation.longitude)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Drone") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header Card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Dispatch Mission",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Assign a drone to emergency coordinates",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ── Drone Selection ──
            Text(
                text = "Select Drone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDroneId.ifBlank { "Select a drone..." },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Flight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    droneList.forEach { drone ->
                        val statusColor = when (drone.status) {
                            "Available" -> Color(0xFF4CAF50)
                            "Dispatched" -> Color(0xFFFF9800)
                            else -> Color.Gray
                        }
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(drone.droneId.ifBlank { "Drone" })
                                    Surface(
                                        color = statusColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = drone.status,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedDroneId = drone.droneId
                                expanded = false
                            }
                        )
                    }
                }
            }

            // ── Target Coordinates ──
            Text(
                text = "Emergency Coordinates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // GPS Auto-fill button
            Button(
                onClick = {
                    if (locationUtils.hasLocationPermission(context)) {
                        locationUtils.requestLocationUpdates(locationVM)
                        userLocation?.let {
                            targetLat = "%.6f".format(it.latitude)
                            targetLng = "%.6f".format(it.longitude)
                        }
                    } else {
                        Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Use Current GPS Location",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = targetLat,
                    onValueChange = { targetLat = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFE53935)
                        )
                    }
                )
                OutlinedTextField(
                    value = targetLng,
                    onValueChange = { targetLng = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5)
                        )
                    }
                )
            }

            // ── Current Location Info ──
            if (userLocation != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Your GPS Location",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            "%.6f, %.6f".format(userLocation.latitude, userLocation.longitude),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Assign Button ──
            Button(
                onClick = {
                    val lat = targetLat.toDoubleOrNull()
                    val lng = targetLng.toDoubleOrNull()

                    if (selectedDroneId.isBlank()) {
                        Toast.makeText(context, "Please select a drone", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (lat == null || lng == null) {
                        Toast.makeText(context, "Invalid coordinates", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isAssigning = true
                    val db = Firebase.firestore

                    // 1. Update drone status to Dispatched
                    db.collection("drones")
                        .document(selectedDroneId)
                        .update(
                            mapOf(
                                "status" to "Dispatched",
                                "assignedIncidentId" to "manual_${System.currentTimeMillis()}"
                            )
                        )

                    // 2. Update target location in Firestore
                    locationViewModel.updateDroneLocation(lat, lng) { success ->
                        isAssigning = false
                        if (success) {
                            Toast.makeText(
                                context,
                                "✅ $selectedDroneId dispatched to (${"%.4f".format(lat)}, ${"%.4f".format(lng)})",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Failed to update location", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isAssigning && selectedDroneId.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B5E20)
                )
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAssigning) "Dispatching..." else "Dispatch Drone",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── WebSocket Telemetry Config Card ──
            var wsUrlInput by remember { mutableStateOf(droneViewModel.webSocketUrl.value) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "WebSocket Server Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "If your laptop's WiFi IP changes, update it here to sync live telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = wsUrlInput,
                            onValueChange = { wsUrlInput = it },
                            label = { Text("Server URL") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (wsUrlInput.isNotBlank()) {
                                    val sharedPref = context.getSharedPreferences("telemetry_prefs", android.content.Context.MODE_PRIVATE)
                                    sharedPref.edit().putString("ws_url", wsUrlInput).apply()
                                    droneViewModel.updateWebSocketUrl(wsUrlInput)
                                    Toast.makeText(context, "Telemetry URL updated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
