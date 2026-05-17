package com.gramasanjeevini.domain

import com.gramasanjeevini.domain.models.Medicine
import com.gramasanjeevini.domain.models.MedicineAvailability
import com.gramasanjeevini.domain.models.Pharmacy
import kotlinx.coroutines.flow.Flow

interface PharmacyRepository {
    fun observePharmacies(): Flow<List<Pharmacy>>

    fun observeMedicines(pharmacyId: String): Flow<List<Medicine>>

    /**
     * Observes a flattened list of medicines across all pharmacies.
     * This makes the Home search UI dead-simple.
     */
    fun observeMedicineAvailabilityAcrossPharmacies(): Flow<List<MedicineAvailability>>

    suspend fun addMedicine(
        pharmacyId: String,
        name: String,
        inStock: Boolean,
        isEmergency: Boolean,
    )

    suspend fun updateMedicine(
        pharmacyId: String,
        medicineId: String,
        name: String,
        inStock: Boolean,
        isEmergency: Boolean,
    )

    suspend fun updateMedicineExpiryAndDiscount(
        pharmacyId: String,
        medicineId: String,
        expiryEpochMillis: Long?,
        discountPercent: Int?,
    )

    suspend fun deleteMedicine(pharmacyId: String, medicineId: String)

    /**
     * Inserts 3+ pharmacies and medicines if Firestore is empty.
     * Safe to call repeatedly.
     */
    suspend fun seedMockDataIfNeeded()
}

