package com.example.disastermanager.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.disastermanager.components.CurrentLocationCard
import com.example.disastermanager.components.DroneStatusQuickCard
import com.example.disastermanager.components.HeaderView
import com.example.disastermanager.components.QuickActionsGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                // Changed from 24.dp to 16.dp to give you more screen real estate
                .padding(horizontal = 16.dp),

            // Reduced vertical gap between cards from 20.dp to 16.dp
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Top spacing
            Spacer(modifier = Modifier.height(12.dp))

            HeaderView(
                modifier = Modifier.fillMaxWidth()
            )

            CurrentLocationCard(
                modifier = Modifier.fillMaxWidth()
            )

            DroneStatusQuickCard(
                modifier = Modifier.fillMaxWidth()
            )

            QuickActionsGrid(
                modifier = Modifier.fillMaxWidth()
            )

            // Bottom spacing so it doesn't hit the very bottom edge
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}