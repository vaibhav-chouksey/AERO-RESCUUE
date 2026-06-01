package com.example.disastermanager.pages.WeatherPage.Weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.disastermanager.pages.WeatherPage.retrofit.NetworkResponse
import com.example.disastermanager.pages.WeatherPage.retrofit.WeatherModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun weatherPage(viewModel: weatherViewModel) {
    var city by remember { mutableStateOf("") }
    val weatherResult = viewModel.weatherResult.observeAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Sleek Search Bar ---
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Search City/Region") },
            placeholder = { Text("Ex: Burhanpur, New Delhi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
            ),
            trailingIcon = {
                IconButton(onClick = {
                    if (city.isNotBlank()) {
                        viewModel.getData(city)
                        focusManager.clearFocus()
                    }
                }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (city.isNotBlank()) {
                    viewModel.getData(city)
                    focusManager.clearFocus()
                }
            })
        )

        // --- 2. Live Weather States ---
        when (val result = weatherResult.value) {
            is NetworkResponse.Error -> {
                ErrorView(message = result.message)
            }
            NetworkResponse.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is NetworkResponse.Success -> {
                WeatherDetails(data = result.data)
            }
            null -> {
                EmptyStateView()
            }
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherModel) {
    val tempVal = data.current.temp_c.toFloatOrNull() ?: 25f
    val isHot = tempVal > 30f
    
    // Dynamic Gradient based on weather temperature
    val gradientColors = if (isHot) {
        listOf(Color(0xFFE65100), Color(0xFFFF9800)) // Warm Golden-Orange (Sunny/Hot)
    } else {
        listOf(Color(0xFF0D47A1), Color(0xFF00B0FF)) // Cool Navy-Teal (Cool/Mild)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Dynamic Temperature Gradient Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.linearGradient(colors = gradientColors))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = data.current.condition.text.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "${data.current.temp_c}°C",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Feels like ${data.current.feelslike_c}°C  •  Wind ${data.current.wind_dir}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Weather Icon with Glowing aura
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(90.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                        AsyncImage(
                            modifier = Modifier.size(68.dp),
                            model = "https:${data.current.condition.icon}".replace("64x64", "128x128"),
                            contentDescription = "Weather condition icon"
                        )
                    }
                }
            }
        }

        // --- 2. Details Grid (Premium Horizontal Row Info Items) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WeatherInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.WaterDrop,
                    label = "Humidity",
                    value = "${data.current.humidity}%",
                    iconColor = Color(0xFF0288D1),
                    iconBgColor = Color(0xFF0288D1).copy(alpha = 0.1f)
                )
                WeatherInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Air,
                    label = "Wind Speed",
                    value = "${data.current.wind_kph} km/h",
                    iconColor = Color(0xFF009688),
                    iconBgColor = Color(0xFF009688).copy(alpha = 0.1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WeatherInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.WbSunny,
                    label = "UV Index",
                    value = data.current.uv,
                    iconColor = Color(0xFFF57C00),
                    iconBgColor = Color(0xFFF57C00).copy(alpha = 0.1f)
                )
                WeatherInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Grain,
                    label = "Precipitation",
                    value = "${data.current.precip_mm} mm",
                    iconColor = Color(0xFF3F51B5),
                    iconBgColor = Color(0xFF3F51B5).copy(alpha = 0.1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WeatherInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.AccessTime,
                    label = "Local Time",
                    value = data.location.localtime.split(" ").getOrNull(1) ?: data.location.localtime,
                    iconColor = Color(0xFF673AB7),
                    iconBgColor = Color(0xFF673AB7).copy(alpha = 0.1f)
                )
                WeatherInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CalendarToday,
                    label = "Date Stamp",
                    value = data.location.localtime.split(" ").getOrNull(0) ?: "",
                    iconColor = Color(0xFFE91E63),
                    iconBgColor = Color(0xFFE91E63).copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun WeatherInfoItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    iconBgColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No Weather Data Loaded",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Search a city above or sync your base GPS location to load real-time meteorological reports.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 24.sp
            )
            Column {
                Text(
                    text = "Sync Error",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}