package com.example.disastermanager.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disastermanager.ViewModel.EmergencyViewModel
import com.example.disastermanager.ViewModel.LocationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // ViewModels
    val locationViewModel: LocationViewModel = viewModel()
    val emergencyViewModel: EmergencyViewModel = viewModel()

    // FETCH REAL-TIME FIREBASE LOCATION
    val latitude by locationViewModel.latitude.collectAsState()
    val longitude by locationViewModel.longitude.collectAsState()

    val scope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }
    var droneAssigned by remember { mutableStateOf(false) }

    // Static report info
    val reportNo = "REP-2025-001"
    val time = "10:30 AM"
    val description = "Slum area submerged; residents displaced to highway."
    val peopleCount = "Approx. 10"
    val supplyNeeded = "Medical Supplies, Food"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Report No", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(reportNo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(time, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // --- DESCRIPTION ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(icon = Icons.Default.Description, title = "Description")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(description, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // --- LOCATION (FETCHED FROM FIREBASE) ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(icon = Icons.Outlined.Map, title = "Location Data")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DataField("Latitude", latitude?.toString() ?: "Loading…")
                            DataField("Longitude", longitude?.toString() ?: "Loading…")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Real-time drone location from Firebase", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            // --- PEOPLE & SUPPLIES ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) { Column(Modifier.padding(16.dp)) { Text("People: $peopleCount") } }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) { Column(Modifier.padding(16.dp)) { Text("Need: $supplyNeeded") } }
            }

            Divider(thickness = 2.dp)

            // --- CONTROL ROOM DISPATCH ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (droneAssigned) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = if (droneAssigned) "Response Active" else "Control Room Action",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (droneAssigned) "Drone dispatched to coordinates." else "Send coordinates to control room.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (droneAssigned || isSending) return@Button

                            if (latitude == null || longitude == null) {
                                Toast.makeText(context, "Location not loaded yet!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSending = true
                            scope.launch {

                                emergencyViewModel.sendEmergencyAlert(
                                    context,
                                    latitude!!,
                                    longitude!!
                                )

                                delay(1200)
                                droneAssigned = true
                                isSending = false
                            }
                        },
                        enabled = !droneAssigned && !isSending,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (droneAssigned) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    ) {

                        when {
                            isSending -> {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Sending…")
                            }
                            droneAssigned -> {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Drone Assigned")
                            }
                            else -> {
                                Icon(Icons.Default.Send, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Dispatch Drone")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// --- Helper Components ---

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DataField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
