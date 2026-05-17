package com.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gramasanjeevini.di.AppGraph

object Factories {
    fun splash(): ViewModelProvider.Factory = simpleFactory {
        SplashViewModel(repository = AppGraph.pharmacyRepository)
    }

    fun auth(): ViewModelProvider.Factory = simpleFactory {
        AuthViewModel(authRepository = AppGraph.authRepository)
    }

    fun home(appState: AppStateViewModel): ViewModelProvider.Factory = simpleFactory {
        HomeViewModel(
            repository = AppGraph.pharmacyRepository,
            appState = appState,
        )
    }

    fun pharmacies(appState: AppStateViewModel): ViewModelProvider.Factory = simpleFactory {
        PharmacyListViewModel(
            repository = AppGraph.pharmacyRepository,
            appState = appState,
        )
    }

    fun admin(): ViewModelProvider.Factory = simpleFactory {
        AdminViewModel(repository = AppGraph.pharmacyRepository)
    }
}

private inline fun <reified VM : ViewModel> simpleFactory(
    crossinline creator: () -> VM,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}

