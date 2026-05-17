package com.gramasanjeevini.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gramasanjeevini.ui.components.EmptyState
import com.gramasanjeevini.ui.components.ErrorState
import com.gramasanjeevini.ui.components.LoadingBlock
import com.gramasanjeevini.ui.models.SearchResultItem
import com.gramasanjeevini.ui.state.UiState
import com.gramasanjeevini.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: HomeViewModel,
) {
    val resultsState by viewModel.resultsState.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Life Saving Drugs") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Emergency availability near you",
                style = MaterialTheme.typography.bodyLarge,
            )

            when (val s = resultsState) {
                UiState.Loading -> LoadingBlock()
                is UiState.Error -> ErrorState(s.message)
                is UiState.Data -> EmergencyList(items = s.value.filter { it.isEmergency })
            }
        }
    }
}

@Composable
private fun EmergencyList(items: List<SearchResultItem>) {
    if (items.isEmpty()) {
        EmptyState(
            title = "No emergency medicines found",
            subtitle = "Try increasing radius or check again later.",
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.medicineName + it.pharmacyId }) { item ->
            // Reuse card UI from HomeScreen by keeping Emergency screen list-based too.
            // This keeps UI consistent and clean.
            EmergencyResultRow(item)
        }
    }
}

@Composable
private fun EmergencyResultRow(item: SearchResultItem) {
    // Minimal row version; Home uses cards. We'll keep it list-based and readable.
    androidx.compose.material3.Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.medicineName, style = MaterialTheme.typography.titleLarge)
            Text("${item.pharmacyName} • ${"%.1f".format(item.distanceKm)} km", style = MaterialTheme.typography.bodyLarge)
            Text(if (item.inStock) "Available now" else "Not available", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

