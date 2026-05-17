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
import androidx.compose.material3.IconButton
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
fun ExpiryWatchScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: HomeViewModel,
    onBack: () -> Unit,
) {
    val resultsState by viewModel.resultsState.collectAsState()
    val now = System.currentTimeMillis()
    val horizon = now + 30L * 24 * 60 * 60 * 1000 // next 30 days

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Expiry Watch") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
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
            Text(
                text = "Medicines expiring soon (next 30 days)",
                style = MaterialTheme.typography.bodyLarge,
            )

            when (val s = resultsState) {
                UiState.Loading -> LoadingBlock()
                is UiState.Error -> ErrorState(s.message)
                is UiState.Data -> {
                    val expiring = s.value
                        .filter { it.expiryEpochMillis != null && it.expiryEpochMillis in now..horizon }
                        .sortedBy { it.expiryEpochMillis }
                    ExpiryList(expiring)
                }
            }
        }
    }
}

@Composable
private fun ExpiryList(items: List<SearchResultItem>) {
    if (items.isEmpty()) {
        EmptyState(
            title = "No near-expiry stock",
            subtitle = "Set expiry dates from Admin → Edit medicine.",
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.medicineName + it.pharmacyId }) { item ->
            androidx.compose.material3.Card {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.medicineName, style = MaterialTheme.typography.titleLarge)
                    Text(item.pharmacyName, style = MaterialTheme.typography.bodyLarge)
                    val exp = item.expiryEpochMillis ?: 0L
                    Text("Expiry: ${iso(exp)}", style = MaterialTheme.typography.bodyMedium)
                    val disc = item.discountPercent?.let { "$it%" } ?: "—"
                    Text("Discount: $disc", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun iso(millis: Long): String {
    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    val y = cal.get(java.util.Calendar.YEAR)
    val m = cal.get(java.util.Calendar.MONTH) + 1
    val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
    return "%04d-%02d-%02d".format(y, m, d)
}

