package com.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramasanjeevini.domain.PharmacyRepository
import com.gramasanjeevini.domain.utils.Geo
import com.gramasanjeevini.ui.models.PharmacyOption
import com.gramasanjeevini.ui.models.SearchResultItem
import com.gramasanjeevini.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    repository: PharmacyRepository,
    appState: AppStateViewModel,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedPharmacyId = MutableStateFlow<String?>(null)
    val selectedPharmacyId: StateFlow<String?> = _selectedPharmacyId.asStateFlow()

    private val _radiusKm = MutableStateFlow(20.0)
    val radiusKm: StateFlow<Double> = _radiusKm.asStateFlow()

    private val pharmaciesFlow = repository.observePharmacies()
    private val availabilityFlow = repository.observeMedicineAvailabilityAcrossPharmacies()

    private data class Filters(
        val query: String,
        val selectedPharmacyId: String?,
        val radiusKm: Double,
        val userLat: Double,
        val userLng: Double,
    )

    private val filtersFlow =
        combine(
            _query,
            _selectedPharmacyId,
            _radiusKm,
            appState.userLatitude,
            appState.userLongitude,
        ) { query, selectedPharmacyId, radiusKm, userLat, userLng ->
            Filters(
                query = query,
                selectedPharmacyId = selectedPharmacyId,
                radiusKm = radiusKm,
                userLat = userLat,
                userLng = userLng,
            )
        }

    val pharmacyOptions: StateFlow<List<PharmacyOption>> =
        pharmaciesFlow
            .combine(availabilityFlow) { pharmacies, _ ->
                pharmacies.map { PharmacyOption(id = it.id, name = it.name) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val resultsState: StateFlow<UiState<List<SearchResultItem>>> =
        combine(availabilityFlow, filtersFlow) { rows, filters ->
            val q = filters.query.trim().lowercase()
            val filtered = rows.asSequence()
                .filter { filters.selectedPharmacyId == null || it.pharmacyId == filters.selectedPharmacyId }
                .map { r ->
                    val d = Geo.distanceKm(
                        lat1 = filters.userLat,
                        lon1 = filters.userLng,
                        lat2 = r.pharmacyLatitude,
                        lon2 = r.pharmacyLongitude,
                    )
                    SearchResultItem(
                        medicineName = r.medicineName,
                        inStock = r.inStock,
                        isEmergency = r.isEmergency,
                        pharmacyId = r.pharmacyId,
                        pharmacyName = r.pharmacyName,
                        distanceKm = d,
                        expiryEpochMillis = r.expiryEpochMillis,
                        discountPercent = r.discountPercent,
                    )
                }
                .filter { it.distanceKm <= filters.radiusKm }
                .filter { q.isEmpty() || it.medicineName.lowercase().contains(q) }
                .sortedWith(
                    compareByDescending<SearchResultItem> { it.isEmergency }
                        .thenByDescending { it.inStock }
                        .thenBy { it.distanceKm }
                        .thenBy { it.medicineName.lowercase() },
                )
                .toList()

            filtered
        }
            .map { UiState.Data(it) as UiState<List<SearchResultItem>> }
            .catch { emit(UiState.Error(it.message ?: "Failed to load medicines")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setSelectedPharmacy(pharmacyId: String?) {
        _selectedPharmacyId.value = pharmacyId
    }

    fun setRadiusKm(value: Double) {
        _radiusKm.value = value
    }
}

