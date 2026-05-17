package com.gramasanjeevini.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gramasanjeevini.data.FirebaseAuthRepository
import com.gramasanjeevini.data.FirebasePharmacyRepository
import com.gramasanjeevini.domain.AuthRepository
import com.gramasanjeevini.domain.PharmacyRepository

/**
 * Tiny manual dependency container (keeps the project simple for students).
 * If you want, you can replace this with Hilt later.
 */
object AppGraph {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(auth = auth, firestore = firestore)
    }

    val pharmacyRepository: PharmacyRepository by lazy {
        FirebasePharmacyRepository(firestore = firestore)
    }
}

