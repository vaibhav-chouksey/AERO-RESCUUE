package com.example.disastermanager.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.disastermanager.ui.theme.GlobalNavigation

// 1. Data Model for a Report
data class DisasterReport(
    val id: String,
    val reportNo: String,
    val timestamp: String,
    val title: String, // Added a title for better UI context
    val status: String // e.g., "Pending", "Resolved"
)

@Composable
fun DisasterReportsPage() {
    val context = LocalContext.current

    // Dummy Data
    val allReports = remember {
        listOf(
            DisasterReport("1", "REP-2025-001", "10:30 AM", "GIET MEDICAL EMERGENCY", "Critical"),
//            DisasterReport("2", "REP-2025-002", "11:15 AM", "Flood Warning - Low Area", "Pending"),
//            DisasterReport("3", "REP-2025-003", "Yesterday", "Tree Fallen on Main Road", "Resolved"),
//            DisasterReport("4", "REP-2025-004", "Yesterday", "Power Grid Failure", "Investigating"),
//            DisasterReport("5", "REP-2025-005", "2 days ago", "Water Supply Contamination", "Pending"),
//            DisasterReport("6", "REP-2025-006", "3 days ago", "Minor Earth Tremors", "Resolved"),
//            DisasterReport("7", "REP-2025-007", "Last Week", "Bridge Crack Reported", "Critical"),
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    // Filter logic
    val filteredReports = if (searchQuery.isBlank()) {
        allReports
    } else {
        allReports.filter {
            it.reportNo.contains(searchQuery, ignoreCase = true) ||
                    it.title.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header & Search Bar ---
        Text(
            text = "Disaster Reports",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Report No or Title...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // --- Report List ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredReports) { report ->
                // Assigning the onClick action here
                ReportCard(
                    report = report,
                    onClick = {
                        // This is where you would navigate to the detail page.
                        // For demonstration, we show a Toast and log the ID.
                        GlobalNavigation.navController.navigate("report_detail")
                        Toast.makeText(context, "Opening ${report.reportNo}", Toast.LENGTH_SHORT).show()
                        Log.d("ReportClick", "Clicked Report ID: ${report.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun ReportCard(report: DisasterReport, onClick: () -> Unit) { // Added onClick lambda
    Card(
        // Apply the clickable modifier to the Card's modifier
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // This makes the entire card clickable
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Increased elevation slightly to suggest clickability
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // --- Top Row: Report No (Left) & Timestamp (Right) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Icon + Report No
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.reportNo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Right Side: Timestamp
                Text(
                    text = report.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // --- Bottom Section: Title & Status Badge ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge (Simple Surface)
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

// Helper to get color based on status
@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        "Critical" -> MaterialTheme.colorScheme.error
        "Resolved" -> Color(0xFF4CAF50) // Green
        "Investigating" -> Color(0xFFFF9800) // Orange
        else -> MaterialTheme.colorScheme.primary
    }
}