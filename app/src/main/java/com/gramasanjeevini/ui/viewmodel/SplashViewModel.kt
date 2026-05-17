package com.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramasanjeevini.domain.PharmacyRepository
import com.gramasanjeevini.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val repository: PharmacyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val state: StateFlow<UiState<Unit>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.seedMockDataIfNeeded()
                _state.value = UiState.Data(Unit)
            } catch (t: Throwable) {
                _state.value = UiState.Error(t.message ?: "Failed to load data")
            }
        }
    }
}

