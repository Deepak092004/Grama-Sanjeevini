package com.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramasanjeevini.domain.AuthRepository
import com.gramasanjeevini.domain.models.AppUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val user: StateFlow<AppUser?> =
        authRepository.observeUser()
            .catch { emit(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun updateEmail(value: String) {
        _form.value = _form.value.copy(email = value)
    }

    fun updatePassword(value: String) {
        _form.value = _form.value.copy(password = value)
    }

    fun updateDisplayName(value: String) {
        _form.value = _form.value.copy(displayName = value)
    }

    fun registerUser() {
        val email = _form.value.email.trim()
        val pass = _form.value.password
        val name = _form.value.displayName.trim()
        if (email.isBlank() || pass.length < 6 || name.isBlank()) {
            viewModelScope.launch { _events.emit("Enter name, valid email, and password (min 6 chars)") }
            return
        }
        viewModelScope.launch {
            try {
                authRepository.registerUser(email = email, password = pass, displayName = name)
            } catch (t: Throwable) {
                _events.emit(t.message ?: "Registration failed")
            }
        }
    }

    fun loginUser() {
        val email = _form.value.email.trim()
        val pass = _form.value.password
        if (email.isBlank() || pass.isBlank()) {
            viewModelScope.launch { _events.emit("Enter email and password") }
            return
        }
        viewModelScope.launch {
            try {
                authRepository.loginUser(email = email, password = pass)
            } catch (t: Throwable) {
                _events.emit(t.message ?: "Login failed")
            }
        }
    }

    fun loginPharmacist() {
        val email = _form.value.email.trim()
        val pass = _form.value.password
        if (email.isBlank() || pass.isBlank()) {
            viewModelScope.launch { _events.emit("Enter email and password") }
            return
        }
        viewModelScope.launch {
            try {
                authRepository.loginPharmacist(email = email, password = pass)
            } catch (t: Throwable) {
                _events.emit(t.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}

