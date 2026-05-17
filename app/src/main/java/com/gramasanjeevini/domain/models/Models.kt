package com.gramasanjeevini.domain.models

data class Pharmacy(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

data class Medicine(
    val id: String,
    val name: String,
    val inStock: Boolean,
    val isEmergency: Boolean,
    val expiryEpochMillis: Long? = null,
    val discountPercent: Int? = null,
)

/**
 * Flattened row for the Search screen.
 * A single medicine result tied to a particular pharmacy.
 */
data class MedicineAvailability(
    val pharmacyId: String,
    val pharmacyName: String,
    val pharmacyLatitude: Double,
    val pharmacyLongitude: Double,
    val medicineId: String,
    val medicineName: String,
    val inStock: Boolean,
    val isEmergency: Boolean,
    val expiryEpochMillis: Long? = null,
    val discountPercent: Int? = null,
)

