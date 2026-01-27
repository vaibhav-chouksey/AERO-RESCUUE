package com.example.disastermanager.pages.WeatherPage.LocationAccess

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disastermanager.pages.WeatherPage.Weather.weatherViewModel


@Composable
fun LocationDisplay(
    viewModelweather: weatherViewModel,
    viewmodellocation: locationViewmodel,
    locationUtils: LocationUtils,
    context: Context
) {
    val location = viewmodellocation.location.value

    // Track the button state dynamically
    val buttonText = when {
        !locationUtils.hasLocationPermission(context) -> "Request Permission"
        location == null -> "Fetching Location..."
        else -> "Location Ready"
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permission granted -> request location
            locationUtils.requestLocationUpdates(viewmodellocation)
        } else {
            Toast.makeText(
                context,
                "Location permission is required",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            if (locationUtils.hasLocationPermission(context)) {
                // Fetch location if permission granted
                locationUtils.requestLocationUpdates(viewmodellocation)
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }) {
            Text(
                text = buttonText,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, Color.Gray, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Update address dynamically when location changes
            val address = location?.let { locationUtils.reverseGeocodeLocation(it) }
            if (address != null) {
                viewModelweather.getData(address) // fetch weather immediately
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Address is $address",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "No Location Detected\nClick Your Location Button To Provide Your Location",
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

