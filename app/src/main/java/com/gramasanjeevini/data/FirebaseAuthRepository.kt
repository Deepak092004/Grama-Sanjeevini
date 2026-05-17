package com.gramasanjeevini.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gramasanjeevini.domain.AuthRepository
import com.gramasanjeevini.domain.models.AppUser
import com.gramasanjeevini.domain.models.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    private val usersCol = firestore.collection("users")

    override fun observeUser(): Flow<AppUser?> = callbackFlow {
        var userListener: com.google.firebase.firestore.ListenerRegistration? = null

        fun clearListener() {
            userListener?.remove()
            userListener = null
        }

        val authListener = FirebaseAuth.AuthStateListener { fbAuth ->
            clearListener()

            val fbUser = fbAuth.currentUser
            if (fbUser == null) {
                trySend(null)
                return@AuthStateListener
            }

            // Observe role/profile from Firestore.
            userListener = usersCol.document(fbUser.uid).addSnapshotListener { snap, _ ->
                val roleStr = snap?.getString("role") ?: "USER"
                val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.USER)
                trySend(
                    AppUser(
                        uid = fbUser.uid,
                        email = fbUser.email,
                        role = role,
                        displayName = snap?.getString("displayName") ?: fbUser.displayName,
                    ),
                )
            }
        }

        auth.addAuthStateListener(authListener)
        awaitClose {
            clearListener()
            auth.removeAuthStateListener(authListener)
        }
    }

    override suspend fun registerUser(email: String, password: String, displayName: String) {
        val res = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = requireNotNull(res.user) { "Registration failed" }
        usersCol.document(user.uid).set(
            mapOf(
                "email" to user.email,
                "displayName" to displayName.trim(),
                "role" to UserRole.USER.name,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    override suspend fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        ensureUserProfile(role = UserRole.USER)
    }

    override suspend fun loginPharmacist(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        // Profile must already have PHARMACIST/ADMIN role (configured in Firestore by you).
        ensureUserProfile(role = UserRole.PHARMACIST)
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun ensureUserProfile(role: UserRole) {
        val fbUser = auth.currentUser ?: return
        val doc = usersCol.document(fbUser.uid).get().await()
        if (doc.exists()) return

        usersCol.document(fbUser.uid).set(
            mapOf(
                "email" to fbUser.email,
                "displayName" to (fbUser.displayName ?: ""),
                "role" to role.name,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            ),
        ).await()
    }
}

