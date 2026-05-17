package com.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramasanjeevini.domain.PharmacyRepository
import com.gramasanjeevini.domain.utils.Geo
import com.gramasanjeevini.ui.state.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PharmacyListItem(
    val id: String,
    val name: String,
    val distanceKm: Double,
)

class PharmacyListViewModel(
    repository: PharmacyRepository,
    appState: AppStateViewModel,
) : ViewModel() {

    val state: StateFlow<UiState<List<PharmacyListItem>>> =
        combine(
            repository.observePharmacies(),
            appState.userLatitude,
            appState.userLongitude,
        ) { pharmacies, userLat, userLng ->
            pharmacies.map { p ->
                PharmacyListItem(
                    id = p.id,
                    name = p.name,
                    distanceKm = Geo.distanceKm(userLat, userLng, p.latitude, p.longitude),
                )
            }.sortedBy { it.distanceKm }
        }
            .map { UiState.Data(it) as UiState<List<PharmacyListItem>> }
            .catch { emit(UiState.Error(it.message ?: "Failed to load pharmacies")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading,
            )
}

