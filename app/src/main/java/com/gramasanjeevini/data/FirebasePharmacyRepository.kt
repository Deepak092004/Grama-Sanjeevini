package com.gramasanjeevini.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.gramasanjeevini.domain.PharmacyRepository
import com.gramasanjeevini.domain.models.Medicine
import com.gramasanjeevini.domain.models.MedicineAvailability
import com.gramasanjeevini.domain.models.Pharmacy
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.ExperimentalCoroutinesApi

class FirebasePharmacyRepository(
    private val firestore: FirebaseFirestore,
) : PharmacyRepository {

    private val pharmaciesCol = firestore.collection("pharmacies")

    override fun observePharmacies(): Flow<List<Pharmacy>> = callbackFlow {
        val registration = pharmaciesCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot.toPharmacies())
        }
        awaitClose { registration.remove() }
    }

    override fun observeMedicines(pharmacyId: String): Flow<List<Medicine>> = callbackFlow {
        val registration = pharmaciesCol.document(pharmacyId)
            .collection("medicines")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val meds = snapshot?.documents.orEmpty().map { doc ->
                    val expiryMillis = doc.getTimestamp("expiryDate")?.toDate()?.time
                    val discountPercent = (doc.getLong("discountPercent") ?: 0L).toInt().takeIf { it > 0 }
                    Medicine(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        inStock = doc.getBoolean("stock") ?: false,
                        isEmergency = doc.getBoolean("isEmergency") ?: false,
                        expiryEpochMillis = expiryMillis,
                        discountPercent = discountPercent,
                    )
                }.sortedBy { it.name.lowercase() }
                trySend(meds)
            }
        awaitClose { registration.remove() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMedicineAvailabilityAcrossPharmacies(): Flow<List<MedicineAvailability>> {
        // Combine pharmacies stream with each pharmacy's medicines stream.
        // For 3-10 pharmacies, this is straightforward and works well.
        return observePharmacies().flatMapLatest { pharmacies ->
            if (pharmacies.isEmpty()) return@flatMapLatest flowOf(emptyList())

            val flows = pharmacies.map { pharmacy ->
                observeMedicines(pharmacy.id).map { meds ->
                    meds.map { m ->
                        MedicineAvailability(
                            pharmacyId = pharmacy.id,
                            pharmacyName = pharmacy.name,
                            pharmacyLatitude = pharmacy.latitude,
                            pharmacyLongitude = pharmacy.longitude,
                            medicineId = m.id,
                            medicineName = m.name,
                            inStock = m.inStock,
                            isEmergency = m.isEmergency,
                            expiryEpochMillis = m.expiryEpochMillis,
                            discountPercent = m.discountPercent,
                        )
                    }
                }
            }

            combine(flows) { lists ->
                lists.toList().flatten()
                    .sortedWith(compareBy({ it.medicineName.lowercase() }, { it.pharmacyName.lowercase() }))
            }
        }
    }

    override suspend fun addMedicine(
        pharmacyId: String,
        name: String,
        inStock: Boolean,
        isEmergency: Boolean,
    ) {
        val data = mapOf(
            "name" to name.trim(),
            "stock" to inStock,
            "isEmergency" to isEmergency,
            // Optional fields (pharmacist can set later).
            "expiryDate" to null,
            "discountPercent" to null,
        )
        pharmaciesCol.document(pharmacyId).collection("medicines").add(data).await()
    }

    override suspend fun updateMedicine(
        pharmacyId: String,
        medicineId: String,
        name: String,
        inStock: Boolean,
        isEmergency: Boolean,
    ) {
        val data = mapOf(
            "name" to name.trim(),
            "stock" to inStock,
            "isEmergency" to isEmergency,
            // Preserve/overwrite optional fields only if present in document already.
            // (Admin screen in this project will manage them in a later step.)
        )
        pharmaciesCol.document(pharmacyId)
            .collection("medicines")
            .document(medicineId)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    override suspend fun updateMedicineExpiryAndDiscount(
        pharmacyId: String,
        medicineId: String,
        expiryEpochMillis: Long?,
        discountPercent: Int?,
    ) {
        val normalizedDiscount = discountPercent?.coerceIn(0, 90)?.takeIf { it > 0 }
        val data = mapOf(
            "expiryDate" to (expiryEpochMillis?.let { com.google.firebase.Timestamp(java.util.Date(it)) }),
            "discountPercent" to normalizedDiscount,
        )
        pharmaciesCol.document(pharmacyId)
            .collection("medicines")
            .document(medicineId)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    override suspend fun deleteMedicine(pharmacyId: String, medicineId: String) {
        pharmaciesCol.document(pharmacyId)
            .collection("medicines")
            .document(medicineId)
            .delete()
            .await()
    }

    override suspend fun seedMockDataIfNeeded() {
        val existing = pharmaciesCol.limit(1).get().await()
        if (!existing.isEmpty) return

        val pharmacies = listOf(
            Pharmacy(
                id = "village_a",
                name = "Village A Medical Store",
                latitude = 12.9716,
                longitude = 77.5946,
            ),
            Pharmacy(
                id = "village_b",
                name = "Village B Health Pharmacy",
                latitude = 12.9616,
                longitude = 77.5846,
            ),
            Pharmacy(
                id = "village_c",
                name = "Village C Jan Aushadhi",
                latitude = 12.9816,
                longitude = 77.6046,
            ),
        )

        for (p in pharmacies) {
            pharmaciesCol.document(p.id).set(
                mapOf(
                    "name" to p.name,
                    "location" to mapOf(
                        "latitude" to p.latitude,
                        "longitude" to p.longitude,
                    ),
                ),
            ).await()

            val medicines = sampleMedicinesFor(p.id)
            for (m in medicines) {
                pharmaciesCol.document(p.id).collection("medicines").add(
                    mapOf(
                        "name" to m.name,
                        "stock" to m.inStock,
                        "isEmergency" to m.isEmergency,
                    ),
                ).await()
            }
        }
    }

    private fun QuerySnapshot?.toPharmacies(): List<Pharmacy> {
        return this?.documents.orEmpty().mapNotNull { doc ->
            val name = doc.getString("name") ?: return@mapNotNull null
            val loc = doc.get("location") as? Map<*, *>
            val lat = (loc?.get("latitude") as? Number)?.toDouble() ?: 0.0
            val lng = (loc?.get("longitude") as? Number)?.toDouble() ?: 0.0
            Pharmacy(
                id = doc.id,
                name = name,
                latitude = lat,
                longitude = lng,
            )
        }.sortedBy { it.name.lowercase() }
    }

    private fun sampleMedicinesFor(pharmacyId: String): List<Medicine> {
        // A small variety, with at least a few emergency meds in each shop.
        val base = listOf(
            Medicine(id = "", name = "Paracetamol 500mg", inStock = true, isEmergency = false),
            Medicine(id = "", name = "Amoxicillin 500mg", inStock = true, isEmergency = false),
            Medicine(id = "", name = "ORS Pack", inStock = true, isEmergency = false),
            Medicine(id = "", name = "Cetirizine 10mg", inStock = false, isEmergency = false),
            Medicine(id = "", name = "Insulin", inStock = true, isEmergency = true),
            Medicine(id = "", name = "Anti-venom", inStock = false, isEmergency = true),
            Medicine(id = "", name = "Salbutamol Inhaler", inStock = true, isEmergency = true),
            Medicine(id = "", name = "Betadine", inStock = true, isEmergency = false),
        )

        // Slight variation per pharmacy so search results show differences.
        return when (pharmacyId) {
            "village_a" -> base
            "village_b" -> base.map {
                if (it.name == "Anti-venom") it.copy(inStock = true) else it
            }
            else -> base.map {
                if (it.name == "Cetirizine 10mg") it.copy(inStock = true) else it
            }
        }
    }
}
