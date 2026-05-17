package com.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramasanjeevini.domain.PharmacyRepository
import com.gramasanjeevini.domain.models.Medicine
import com.gramasanjeevini.ui.state.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class AdminFormState(
    val editingMedicineId: String? = null,
    val name: String = "",
    val inStock: Boolean = true,
    val isEmergency: Boolean = false,
    val expiryDateIso: String = "", // YYYY-MM-DD (optional)
    val discountPercent: String = "", // optional number (0-90)
)

class AdminViewModel(
    private val repository: PharmacyRepository,
) : ViewModel() {

    private val _selectedPharmacyId = MutableStateFlow<String?>(null)
    val selectedPharmacyId: StateFlow<String?> = _selectedPharmacyId.asStateFlow()

    private val _form = MutableStateFlow(AdminFormState())
    val form: StateFlow<AdminFormState> = _form.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    val pharmacies: StateFlow<UiState<List<PharmacyListItem>>> =
        repository.observePharmacies()
            .map { list -> list.map { PharmacyListItem(it.id, it.name, 0.0) } }
            .map { UiState.Data(it) as UiState<List<PharmacyListItem>> }
            .catch { emit(UiState.Error(it.message ?: "Failed to load pharmacies")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val medicines: StateFlow<UiState<List<Medicine>>> =
        _selectedPharmacyId
            .flatMapLatest { pharmacyId ->
                if (pharmacyId.isNullOrBlank()) {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                } else {
                    repository.observeMedicines(pharmacyId)
                }
            }
            .map { UiState.Data(it) as UiState<List<Medicine>> }
            .catch { emit(UiState.Error(it.message ?: "Failed to load medicines")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun selectPharmacy(pharmacyId: String?) {
        _selectedPharmacyId.value = pharmacyId
        clearForm()
    }

    fun startEdit(medicine: Medicine) {
        _form.value = AdminFormState(
            editingMedicineId = medicine.id,
            name = medicine.name,
            inStock = medicine.inStock,
            isEmergency = medicine.isEmergency,
            expiryDateIso = medicine.expiryEpochMillis?.let { millisToIsoDate(it) }.orEmpty(),
            discountPercent = medicine.discountPercent?.toString().orEmpty(),
        )
    }

    fun updateName(value: String) {
        _form.value = _form.value.copy(name = value)
    }

    fun toggleInStock(value: Boolean) {
        _form.value = _form.value.copy(inStock = value)
    }

    fun toggleEmergency(value: Boolean) {
        _form.value = _form.value.copy(isEmergency = value)
    }

    fun clearForm() {
        _form.value = AdminFormState()
    }

    fun updateExpiryDateIso(value: String) {
        _form.value = _form.value.copy(expiryDateIso = value)
    }

    fun updateDiscountPercent(value: String) {
        _form.value = _form.value.copy(discountPercent = value)
    }

    fun save() {
        val pharmacyId = _selectedPharmacyId.value
        if (pharmacyId.isNullOrBlank()) {
            viewModelScope.launch { _events.emit("Select a pharmacy first") }
            return
        }

        val form = _form.value
        val name = form.name.trim()
        if (name.isBlank()) {
            viewModelScope.launch { _events.emit("Medicine name cannot be empty") }
            return
        }

        viewModelScope.launch {
            try {
                val editingId = form.editingMedicineId
                if (editingId == null) {
                    repository.addMedicine(
                        pharmacyId = pharmacyId,
                        name = name,
                        inStock = form.inStock,
                        isEmergency = form.isEmergency,
                    )
                    _events.emit("Medicine added")
                } else {
                    repository.updateMedicine(
                        pharmacyId = pharmacyId,
                        medicineId = editingId,
                        name = name,
                        inStock = form.inStock,
                        isEmergency = form.isEmergency,
                    )
                    val expiryMillis = isoDateToMillisOrNull(form.expiryDateIso)
                    val discount = form.discountPercent.trim().toIntOrNull()
                    repository.updateMedicineExpiryAndDiscount(
                        pharmacyId = pharmacyId,
                        medicineId = editingId,
                        expiryEpochMillis = expiryMillis,
                        discountPercent = discount,
                    )
                    _events.emit("Medicine updated")
                }
                clearForm()
            } catch (t: Throwable) {
                _events.emit(t.message ?: "Save failed")
            }
        }
    }

    fun delete(pharmacyId: String, medicineId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMedicine(pharmacyId, medicineId)
                _events.emit("Medicine deleted")
                if (_form.value.editingMedicineId == medicineId) clearForm()
            } catch (t: Throwable) {
                _events.emit(t.message ?: "Delete failed")
            }
        }
    }

    private fun isoDateToMillisOrNull(iso: String): Long? {
        val v = iso.trim()
        if (v.isBlank()) return null
        return try {
            val parts = v.split("-")
            if (parts.size != 3) return null
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            cal.set(java.util.Calendar.YEAR, y)
            cal.set(java.util.Calendar.MONTH, m - 1)
            cal.set(java.util.Calendar.DAY_OF_MONTH, d)
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        } catch (_: Throwable) {
            null
        }
    }

    private fun millisToIsoDate(millis: Long): String {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millis
        val y = cal.get(java.util.Calendar.YEAR)
        val m = cal.get(java.util.Calendar.MONTH) + 1
        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }
}

