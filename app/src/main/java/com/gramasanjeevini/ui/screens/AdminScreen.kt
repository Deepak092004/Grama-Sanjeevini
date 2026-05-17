package com.gramasanjeevini.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gramasanjeevini.domain.models.Medicine
import com.gramasanjeevini.ui.components.EmptyState
import com.gramasanjeevini.ui.components.EmergencyBadge
import com.gramasanjeevini.ui.components.ErrorState
import com.gramasanjeevini.ui.components.LoadingBlock
import com.gramasanjeevini.ui.components.StockChip
import com.gramasanjeevini.ui.state.UiState
import com.gramasanjeevini.ui.viewmodel.AdminViewModel
import com.gramasanjeevini.ui.viewmodel.PharmacyListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: AdminViewModel,
    onBack: () -> Unit,
) {
    val selectedPharmacyId by viewModel.selectedPharmacyId.collectAsState()
    val form by viewModel.form.collectAsState()
    val pharmaciesState by viewModel.pharmacies.collectAsState()
    val medicinesState by viewModel.medicines.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Admin panel") },
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
            when (val s = pharmaciesState) {
                UiState.Loading -> LoadingBlock("Loading shops...")
                is UiState.Error -> ErrorState(s.message)
                is UiState.Data -> PharmacyPicker(
                    items = s.value,
                    selectedId = selectedPharmacyId,
                    onSelect = viewModel::selectPharmacy,
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = if (form.editingMedicineId == null) "Add medicine" else "Edit medicine",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = form.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Medicine name") },
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("In stock", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(10.dp))
                            androidx.compose.material3.Switch(checked = form.inStock, onCheckedChange = viewModel::toggleInStock)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Emergency", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(10.dp))
                            androidx.compose.material3.Switch(checked = form.isEmergency, onCheckedChange = viewModel::toggleEmergency)
                        }
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = form.expiryDateIso,
                        onValueChange = viewModel::updateExpiryDateIso,
                        label = { Text("Expiry date (YYYY-MM-DD) - optional") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = form.discountPercent,
                        onValueChange = viewModel::updateDiscountPercent,
                        label = { Text("Discount % for near-expiry - optional") },
                        singleLine = true,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = viewModel::save) {
                            Text(if (form.editingMedicineId == null) "Add" else "Update")
                        }
                        Button(onClick = viewModel::clearForm) {
                            Text("Clear")
                        }
                    }
                }
            }

            when (val s = medicinesState) {
                UiState.Loading -> LoadingBlock("Loading medicines...")
                is UiState.Error -> ErrorState(s.message)
                is UiState.Data -> MedicinesList(
                    medicines = s.value,
                    selectedPharmacyId = selectedPharmacyId,
                    isAdmin = true,
                    onEdit = viewModel::startEdit,
                    onDelete = { medId ->
                        val pid = selectedPharmacyId ?: return@MedicinesList
                        viewModel.delete(pid, medId)
                    },
                )
            }
        }
    }
}

@Composable
private fun PharmacyPicker(
    items: List<PharmacyListItem>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = items.firstOrNull { it.id == selectedId }?.name ?: "Select a shop"

    Column {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            value = selectedName,
            onValueChange = {},
            label = { Text("Shop") },
            trailingIcon = {
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { p ->
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
private fun MedicinesList(
    medicines: List<Medicine>,
    selectedPharmacyId: String?,
    isAdmin: Boolean,
    onEdit: (Medicine) -> Unit,
    onDelete: (String) -> Unit,
) {
    if (selectedPharmacyId.isNullOrBlank()) {
        EmptyState(
            title = "Select a shop",
            subtitle = "Choose a pharmacy to manage its medicines.",
        )
        return
    }

    if (medicines.isEmpty()) {
        EmptyState(
            title = "No medicines",
            subtitle = "Add medicines using the form above.",
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(medicines, key = { it.id }) { med ->
            Card(modifier = Modifier.fillMaxWidth()) {
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
                            Text(med.name, style = MaterialTheme.typography.titleLarge)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                StockChip(inStock = med.inStock)
                                if (med.isEmergency) EmergencyBadge()
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { if (isAdmin) onEdit(med) }, enabled = isAdmin) {
                            Text("Edit")
                        }
                        Button(onClick = { if (isAdmin) onDelete(med.id) }, enabled = isAdmin) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.width(1.dp)) }
    }
}

