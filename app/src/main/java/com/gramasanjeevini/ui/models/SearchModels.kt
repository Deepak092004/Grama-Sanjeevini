package com.gramasanjeevini.ui.models

data class PharmacyOption(
    val id: String,
    val name: String,
)

data class SearchResultItem(
    val medicineName: String,
    val inStock: Boolean,
    val isEmergency: Boolean,
    val pharmacyId: String,
    val pharmacyName: String,
    val distanceKm: Double,
    val expiryEpochMillis: Long? = null,
    val discountPercent: Int? = null,
)

