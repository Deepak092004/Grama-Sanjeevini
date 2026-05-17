package com.gramasanjeevini.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import com.gramasanjeevini.ui.components.EmptyState
import com.gramasanjeevini.ui.components.EmergencyBadge
import com.gramasanjeevini.ui.components.ErrorState
import com.gramasanjeevini.ui.components.LoadingBlock
import com.gramasanjeevini.ui.components.StockChip
import com.gramasanjeevini.ui.models.PharmacyOption
import com.gramasanjeevini.ui.models.SearchResultItem
import com.gramasanjeevini.ui.state.UiState
import com.gramasanjeevini.ui.viewmodel.AppStateViewModel
import com.gramasanjeevini.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    appState: AppStateViewModel,
    viewModel: HomeViewModel,
    onOpenPharmacies: () -> Unit,
    onOpenAdmin: () -> Unit,
) {
    val query by viewModel.query.collectAsState()
    val selectedPharmacyId by viewModel.selectedPharmacyId.collectAsState()
    val radiusKm by viewModel.radiusKm.collectAsState()
    val pharmacies by viewModel.pharmacyOptions.collectAsState()
    val resultsState by viewModel.resultsState.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Grama-Sanjeevini") },
                actions = {
                    Button(onClick = onOpenPharmacies, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Shops")
                    }
                    IconButton(onClick = onOpenAdmin) {
                        Text("Manage")
                    }
                },
                scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior(
                    rememberTopAppBarState(),
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search medicine name") },
                singleLine = true,
            )

            PharmacyDropdown(
                pharmacies = pharmacies,
                selectedId = selectedPharmacyId,
                onSelect = viewModel::setSelectedPharmacy,
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Radius: ${radiusKm.toInt()} km",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Slider(
                    value = radiusKm.toFloat(),
                    onValueChange = { viewModel.setRadiusKm(it.toDouble()) },
                    valueRange = 5f..30f,
                )
            }

            when (val s = resultsState) {
                UiState.Loading -> LoadingBlock()
                is UiState.Error -> ErrorState(s.message)
                is UiState.Data -> ResultsList(items = s.value)
            }
        }
    }
}

@Composable
private fun PharmacyDropdown(
    pharmacies: List<PharmacyOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = pharmacies.firstOrNull { it.id == selectedId }?.name ?: "All nearby shops"

    Column {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            value = selectedName,
            onValueChange = {},
            label = { Text("Shop filter") },
            trailingIcon = {
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            enabled = true,
        )
        // Simple dropdown (works on all Material3 versions).
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All nearby shops") },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            pharmacies.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.name) },
                    onClick = {
                        onSelect(p.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ResultsList(items: List<SearchResultItem>) {
    if (items.isEmpty()) {
        EmptyState(
            title = "No medicines found",
            subtitle = "Try a different name or increase radius.",
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items, key = { it.medicineName + it.pharmacyId }) { item ->
            MedicineResultCard(item = item)
        }
        item { Spacer(Modifier.width(1.dp)) }
    }
}

@Composable
private fun MedicineResultCard(item: SearchResultItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.medicineName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "${item.pharmacyName} • ${"%.1f".format(item.distanceKm)} km",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (item.isEmergency) {
                    EmergencyBadge()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StockChip(inStock = item.inStock)
                if (!item.inStock) {
                    Text(
                        text = "Ask nearby shops or check tomorrow",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

