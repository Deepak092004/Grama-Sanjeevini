package com.gramasanjeevini.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gramasanjeevini.ui.components.EmptyState
import com.gramasanjeevini.ui.components.ErrorState
import com.gramasanjeevini.ui.components.LoadingBlock
import com.gramasanjeevini.ui.state.UiState
import com.gramasanjeevini.ui.viewmodel.PharmacyListItem
import com.gramasanjeevini.ui.viewmodel.PharmacyListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyListScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: PharmacyListViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nearby shops") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (val s = state) {
                UiState.Loading -> LoadingBlock()
                is UiState.Error -> ErrorState(s.message)
                is UiState.Data -> PharmacyList(items = s.value)
            }
        }
    }
}

@Composable
private fun PharmacyList(items: List<PharmacyListItem>) {
    if (items.isEmpty()) {
        EmptyState(
            title = "No shops found",
            subtitle = "Check your internet connection and try again.",
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items, key = { it.id }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Distance: ${"%.1f".format(item.distanceKm)} km",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

