package com.gramasanjeevini.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Report
import com.google.android.gms.location.LocationServices
import com.gramasanjeevini.domain.models.UserRole
import com.gramasanjeevini.ui.viewmodel.AppStateViewModel
import com.gramasanjeevini.ui.viewmodel.AuthViewModel
import com.gramasanjeevini.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.tasks.await

private data class Tab(
    val label: String,
    val icon: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    snackbarHostState: SnackbarHostState,
    appState: AppStateViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    onOpenPharmacies: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenExpiryWatch: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val user by authViewModel.user.collectAsStateWithLifecycle()
    val role = user?.role ?: UserRole.USER
    val context = LocalContext.current

    LaunchedEffect(user) {
        if (user == null) onLoggedOut()
    }

    val tabs = remember(role) {
        buildList {
            add(Tab("Search") { Icon(Icons.Outlined.Medication, contentDescription = null) })
            add(Tab("Emergency") { Icon(Icons.Outlined.Report, contentDescription = null) })
            add(Tab("Profile") { Icon(Icons.Outlined.Person, contentDescription = null) })
            if (role == UserRole.PHARMACIST || role == UserRole.ADMIN) {
                add(Tab("Admin") { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null) })
            }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* handled in effect */ },
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Best-effort location update (falls back to default coordinates if not granted).
    LaunchedEffect(Unit) {
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            @SuppressLint("MissingPermission")
            val loc = client.lastLocation.await()
            if (loc != null) {
                appState.setUserLocation(loc.latitude, loc.longitude)
            }
        } catch (_: Throwable) {
            // Ignore; app continues with default/mock location.
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { idx, tab ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        icon = tab.icon,
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (tabs.getOrNull(selectedTab)?.label) {
                "Search" -> HomeScreen(
                    snackbarHostState = snackbarHostState,
                    appState = appState,
                    viewModel = homeViewModel,
                    onOpenPharmacies = onOpenPharmacies,
                    onOpenAdmin = onOpenAdmin,
                )
                "Emergency" -> EmergencyScreen(
                    snackbarHostState = snackbarHostState,
                    viewModel = homeViewModel,
                )
                "Profile" -> ProfileScreen(
                    snackbarHostState = snackbarHostState,
                    authViewModel = authViewModel,
                    role = role,
                    onOpenExpiryWatch = onOpenExpiryWatch,
                )
                "Admin" -> AdminGateScreen(
                    snackbarHostState = snackbarHostState,
                    role = role,
                    onOpenAdmin = onOpenAdmin,
                )
            }
        }
    }
}

