package com.example.disastermanager.pages

// Your custom imports
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disastermanager.pages.WeatherPage.LocationAccess.LocationDisplay
import com.example.disastermanager.pages.WeatherPage.LocationAccess.LocationUtils
import com.example.disastermanager.pages.WeatherPage.LocationAccess.locationViewmodel
import com.example.disastermanager.pages.WeatherPage.Weather.weatherPage
import com.example.disastermanager.pages.WeatherPage.Weather.weatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherPage(modifier: Modifier = Modifier) {
    val weatherViewModel: weatherViewModel = viewModel()
    val locationViewModel: locationViewmodel = viewModel()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Weather & Location") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // 1. Respects the TopBar height
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp), // 2. Adds consistent outer padding (Top/Bottom/Sides)
            verticalArrangement = Arrangement.spacedBy(16.dp) // 3. Adds space betweeen the Cards
        ) {
            // REMOVED: The top Spacer was deleted because .padding(vertical = 16.dp) handles it now.

            // 1. Location Section
//            MyApp(locationViewModel, weatherViewModel)

            // 2. Weather Details Section
            weatherPage(weatherViewModel)

            // Optional: Extra breathing room at the very bottom of the scroll
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MyApp(
    viewmodellocation: locationViewmodel,
    viewModelWeather: weatherViewModel
) {
    val context = LocalContext.current
    // It's good practice to remember this if possible, but strictly optional here
    val locationUtils = LocationUtils(context)

    LocationDisplay(
        context = context,
        locationUtils = locationUtils,
        viewModelweather = viewModelWeather,
        viewmodellocation = viewmodellocation
    )
}