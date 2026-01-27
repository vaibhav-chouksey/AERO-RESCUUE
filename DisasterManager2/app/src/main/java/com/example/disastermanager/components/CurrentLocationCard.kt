package com.example.disastermanager.components

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disastermanager.ViewModel.AuthViewModel
import com.example.disastermanager.pages.WeatherPage.LocationAccess.LocationUtils
import com.example.disastermanager.pages.WeatherPage.LocationAccess.locationViewmodel
import com.example.disastermanager.pages.WeatherPage.Weather.weatherViewModel

@Composable
fun CurrentLocationCard(
    modifier: Modifier = Modifier
) {
    val weatherViewModel: weatherViewModel = viewModel()
    val locationViewModel: locationViewmodel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val context = LocalContext.current
    val locationUtils = LocationUtils(context)

    // ===== STATE =====
    val location = locationViewModel.location.value
    var address by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // ===== PERMISSION HANDLER =====
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            isLoading = true
            locationUtils.requestLocationUpdates(locationViewModel)
        } else {
            isLoading = false
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_LONG).show()
        }
    }


    // AUTO RUN WHEN LOCATION CHANGES


    LaunchedEffect(location) {
        if (location != null) {

            // Reverse geocode
            val newAddress = locationUtils.reverseGeocodeLocation(location)
            address = newAddress

            // Update weather
            newAddress?.let {
                weatherViewModel.getData(it)
            }

            // Update Firestore
            val uid = authViewModel.getCurrentUserId()
            if (uid != null) {
                authViewModel.saveUserLocation(
                    uid = uid,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = newAddress
                )
            }

            // Stop loading
            isLoading = false
        }
    }

    // =====================================================
    //                   UI CARD
    // =====================================================

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---- HEADER ----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Current Location", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))

            // ---- ADDRESS DISPLAY ----
            Text(
                text = address ?: "Location not detected yet.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(24.dp))

            // ---- BUTTON ----
            Button(
                onClick = {
                    isLoading = true

                    if (locationUtils.hasLocationPermission(context)) {
                        locationUtils.requestLocationUpdates(locationViewModel)
                    } else {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Fetching Location...")
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (address == null) "Provide Location" else "Update Location")
                }
            }
        }
    }
}
