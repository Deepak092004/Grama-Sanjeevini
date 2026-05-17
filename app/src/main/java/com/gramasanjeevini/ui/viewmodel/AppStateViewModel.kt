package com.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateViewModel : ViewModel() {
    // User location (starts with a sensible default; overwritten when permission granted).
    private val _userLatitude = MutableStateFlow(12.9716)
    private val _userLongitude = MutableStateFlow(77.5946)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLatitude.value = latitude
        _userLongitude.value = longitude
    }
}

