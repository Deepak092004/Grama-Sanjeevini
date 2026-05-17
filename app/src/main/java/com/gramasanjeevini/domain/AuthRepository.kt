package com.gramasanjeevini.domain

import com.gramasanjeevini.domain.models.AppUser
import com.gramasanjeevini.domain.models.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeUser(): Flow<AppUser?>

    suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
    )

    suspend fun loginUser(email: String, password: String)

    suspend fun loginPharmacist(email: String, password: String)

    suspend fun logout()

    suspend fun ensureUserProfile(role: UserRole)
}

