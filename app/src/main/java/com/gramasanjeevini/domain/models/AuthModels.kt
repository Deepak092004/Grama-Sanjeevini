package com.gramasanjeevini.domain.models

enum class UserRole {
    USER,
    PHARMACIST,
    ADMIN,
}

data class AppUser(
    val uid: String,
    val email: String?,
    val role: UserRole,
    val displayName: String? = null,
)

