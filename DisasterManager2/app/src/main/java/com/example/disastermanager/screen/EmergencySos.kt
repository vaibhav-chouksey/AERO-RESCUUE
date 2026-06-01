package com.example.disastermanager.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disastermanager.ViewModel.EmergencyViewModel
import com.example.disastermanager.pages.WeatherPage.LocationAccess.LocationUtils
import com.example.disastermanager.pages.WeatherPage.LocationAccess.locationViewmodel
import kotlinx.coroutines.delay

@Composable
fun EmergencySOSPage(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val locationViewModel: locationViewmodel = viewModel()
    val emergencyViewModel: EmergencyViewModel = viewModel()
    val locationUtils = remember { LocationUtils(context) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(5) }
    var isSending by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (locationUtils.hasLocationPermission(context)) {
            locationUtils.requestLocationUpdates(locationViewModel)
        }
    }

    LaunchedEffect(isCountingDown) {
        if (!isCountingDown) return@LaunchedEffect

        countdown = 5
        while (countdown > 0 && isCountingDown) {
            delay(1000)
            countdown -= 1
        }

        if (isCountingDown && countdown == 0) {
            val location = locationViewModel.location.value
            if (location == null) {
                isCountingDown = false
                Toast.makeText(context, "GPS location unavailable. Please try again.", Toast.LENGTH_SHORT).show()
                return@LaunchedEffect
            }

            isCountingDown = false
            isSending = true
            emergencyViewModel.sendEmergencyAlert(
                context = context,
                latitude = location.latitude,
                longitude = location.longitude,
                emergencyType = "SOS"
            ) { success ->
                isSending = false
                resultMessage = if (success) "SOS registered and dispatch started" else "SOS failed. Please try again"
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Send emergency SOS?") },
            text = { Text("Your live GPS location will be sent to the emergency response team.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        resultMessage = null
                        isCountingDown = true
                    }
                ) {
                    Text("Start Countdown")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "EMERGENCY MODE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Only use in case of immediate danger",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                )

                Surface(
                    modifier = Modifier
                        .size(180.dp)
                        .shadow(elevation = 10.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable(enabled = !isCountingDown && !isSending) {
                            val location = locationViewModel.location.value
                            if (location == null) {
                                Toast.makeText(context, "Getting location. Please wait", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            showConfirmDialog = true
                        },
                    color = if (isCountingDown) Color(0xFFFF9800) else MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isCountingDown) Icons.Default.Close else Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (isCountingDown) countdown.toString() else "SOS",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when {
                                isSending -> "SENDING"
                                isCountingDown -> "COUNTDOWN"
                                else -> "TAP TO CALL"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (isCountingDown) {
                OutlinedButton(
                    onClick = {
                        isCountingDown = false
                        resultMessage = "SOS cancelled"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel SOS")
                }
            }

            resultMessage?.let {
                Text(
                    text = it,
                    color = if (it.contains("failed", ignoreCase = true)) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "What happens next?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    EmergencyFeatureRow(Icons.Default.Call, "Registers an SOS in Firestore")
                    EmergencyFeatureRow(Icons.Default.MyLocation, "Sends your live GPS coordinates")
                }
            }

            Text(
                text = "Ensure GPS is enabled for accurate tracking.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmergencyFeatureRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
