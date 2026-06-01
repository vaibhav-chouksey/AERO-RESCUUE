package com.example.disastermanager.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disastermanager.model.DisasterReport
import com.example.disastermanager.pages.WeatherPage.LocationAccess.LocationUtils
import com.example.disastermanager.pages.WeatherPage.LocationAccess.locationViewmodel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterReportsPage(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val locationViewModel: locationViewmodel = viewModel()
    val locationUtils = remember { LocationUtils(context) }
    val reports = remember { mutableStateListOf<DisasterReport>() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var disasterType by remember { mutableStateOf("Flood") }
    var emergencyLevel by remember { mutableStateOf("Medium") }
    var imageBase64 by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (locationUtils.hasLocationPermission(context)) {
            locationUtils.requestLocationUpdates(locationViewModel)
        }
    }

    DisposableEffect(Unit) {
        val registration = db.collection("disaster_reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load reports", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                reports.clear()
                snapshot?.documents?.forEach { document ->
                    document.toObject(DisasterReport::class.java)?.let {
                        reports.add(it.copy(id = document.id))
                    }
                }
            }

        onDispose { registration.remove() }
    }

    val filteredReports = reports.filter { report ->
        val matchesQuery = searchQuery.isBlank() ||
            report.reportNo.contains(searchQuery, ignoreCase = true) ||
            report.title.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" ||
            report.status == selectedFilter ||
            report.disasterType == selectedFilter

        matchesQuery && matchesFilter
    }

    fun submitReport() {
        val location = locationViewModel.location.value
        val user = Firebase.auth.currentUser

        if (title.isBlank() || description.isBlank()) {
            Toast.makeText(context, "Please add title and description", Toast.LENGTH_SHORT).show()
            return
        }

        if (location == null) {
            Toast.makeText(context, "Getting GPS location. Try again in a moment.", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true
        val timestamp = System.currentTimeMillis()
        val reportNo = "REP-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date(timestamp))}"
        val report = DisasterReport(
            reportNo = reportNo,
            timestamp = timestamp,
            title = title.trim(),
            description = description.trim(),
            disasterType = disasterType,
            emergencyLevel = emergencyLevel,
            status = "Pending",
            imageBase64 = imageBase64,
            latitude = location.latitude,
            longitude = location.longitude,
            userId = user?.uid.orEmpty(),
            userName = user?.displayName ?: user?.email.orEmpty()
        )

        db.collection("disaster_reports")
            .add(report)
            .addOnSuccessListener {
                title = ""
                description = ""
                imageBase64 = ""
                isSubmitting = false
                Toast.makeText(context, "Disaster report submitted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                isSubmitting = false
                Toast.makeText(context, "Report submission failed", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disaster Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                ReportSubmissionCard(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    disasterType = disasterType,
                    onDisasterTypeChange = { disasterType = it },
                    emergencyLevel = emergencyLevel,
                    onEmergencyLevelChange = { emergencyLevel = it },
                    imageBase64 = imageBase64,
                    onSimulateImage = { imageBase64 = "simulated-image-${System.currentTimeMillis()}" },
                    locationText = locationViewModel.location.value?.let {
                        String.format(Locale.getDefault(), "%.5f, %.5f", it.latitude, it.longitude)
                    } ?: "Waiting for GPS",
                    isSubmitting = isSubmitting,
                    onSubmit = { submitReport() }
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search report no or title") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OptionDropdown(
                        label = "Filter",
                        value = selectedFilter,
                        options = listOf("All", "Pending", "Critical", "Resolved", "Flood", "Fire", "Accident", "Medical"),
                        onValueChange = { selectedFilter = it }
                    )
                }
            }

            items(filteredReports) { report ->
                ReportCard(
                    report = report,
                    onClick = { navController.navigate("report_detail/${report.id}") }
                )
            }
        }
    }
}

@Composable
private fun ReportSubmissionCard(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    disasterType: String,
    onDisasterTypeChange: (String) -> Unit,
    emergencyLevel: String,
    onEmergencyLevelChange: (String) -> Unit,
    imageBase64: String,
    onSimulateImage: () -> Unit,
    locationText: String,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Submit New Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description") },
                minLines = 3
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OptionDropdown(
                    label = "Type",
                    value = disasterType,
                    options = listOf("Flood", "Fire", "Accident", "Medical"),
                    onValueChange = onDisasterTypeChange,
                    modifier = Modifier.weight(1f)
                )
                OptionDropdown(
                    label = "Level",
                    value = emergencyLevel,
                    options = listOf("Low", "Medium", "High", "Critical"),
                    onValueChange = onEmergencyLevelChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(locationText, style = MaterialTheme.typography.bodyMedium)
            }

            Button(onClick = onSimulateImage) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (imageBase64.isBlank()) "Simulate Image" else "Image Attached")
            }

            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSubmitting) "Submitting..." else "Submit Report")
            }
        }
    }
}

@Composable
private fun OptionDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) },
            readOnly = true,
            enabled = false
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ReportCard(report: DisasterReport, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.reportNo.ifBlank { report.id },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = formatReportTime(report.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${report.disasterType} • ${report.emergencyLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = getStatusColor(report.status).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = report.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = getStatusColor(report.status)
                    )
                }
            }
        }
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        "Critical" -> MaterialTheme.colorScheme.error
        "Resolved" -> Color(0xFF4CAF50)
        "Investigating", "Assigned" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun formatReportTime(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
