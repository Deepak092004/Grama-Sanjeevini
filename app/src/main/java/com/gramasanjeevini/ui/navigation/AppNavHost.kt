package com.gramasanjeevini.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gramasanjeevini.ui.screens.AdminScreen
import com.gramasanjeevini.ui.screens.ExpiryWatchScreen
import com.gramasanjeevini.ui.screens.HomeScreen
import com.gramasanjeevini.ui.screens.MainScreen
import com.gramasanjeevini.ui.screens.PharmacyListScreen
import com.gramasanjeevini.ui.screens.SplashScreen
import com.gramasanjeevini.ui.screens.auth.AuthChoiceScreen
import com.gramasanjeevini.ui.screens.auth.PharmacistLoginScreen
import com.gramasanjeevini.ui.screens.auth.RegisterScreen
import com.gramasanjeevini.ui.screens.auth.UserLoginScreen
import com.gramasanjeevini.ui.viewmodel.AppStateViewModel
import com.gramasanjeevini.ui.viewmodel.Factories

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState,
) {
    val appState: AppStateViewModel = viewModel()
    val authViewModel = viewModel<com.gramasanjeevini.ui.viewmodel.AuthViewModel>(factory = Factories.auth())

    // Single host for snackbars across the app.
    LaunchedEffect(Unit) {
        // no-op: here if you later want global messages
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                snackbarHostState = snackbarHostState,
                viewModel = viewModel(factory = Factories.splash()),
                authViewModel = authViewModel,
                onGoToAuth = {
                    navController.navigate(Routes.AuthChoice) {
                        popUpTo(Routes.Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoToMain = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.AuthChoice) {
            AuthChoiceScreen(
                snackbarHostState = snackbarHostState,
                onUserLogin = { navController.navigate(Routes.UserLogin) },
                onUserRegister = { navController.navigate(Routes.UserRegister) },
                onPharmacistLogin = { navController.navigate(Routes.PharmacistLogin) },
            )
        }

        composable(Routes.UserLogin) {
            UserLoginScreen(
                snackbarHostState = snackbarHostState,
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLoggedIn = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.AuthChoice) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.UserRegister) {
            RegisterScreen(
                snackbarHostState = snackbarHostState,
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLoggedIn = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.AuthChoice) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.PharmacistLogin) {
            PharmacistLoginScreen(
                snackbarHostState = snackbarHostState,
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLoggedIn = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.AuthChoice) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.Main) {
            MainScreen(
                snackbarHostState = snackbarHostState,
                appState = appState,
                authViewModel = authViewModel,
                homeViewModel = viewModel(factory = Factories.home(appState)),
                onOpenPharmacies = { navController.navigate(Routes.Pharmacies) },
                onOpenAdmin = { navController.navigate(Routes.Admin) },
                onOpenExpiryWatch = { navController.navigate(Routes.ExpiryWatch) },
                onLoggedOut = {
                    navController.navigate(Routes.AuthChoice) {
                        popUpTo(Routes.Main) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.Pharmacies) {
            PharmacyListScreen(
                snackbarHostState = snackbarHostState,
                viewModel = viewModel(factory = Factories.pharmacies(appState)),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Admin) {
            AdminScreen(
                snackbarHostState = snackbarHostState,
                viewModel = viewModel(factory = Factories.admin()),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ExpiryWatch) {
            ExpiryWatchScreen(
                snackbarHostState = snackbarHostState,
                viewModel = viewModel(factory = Factories.home(appState)),
                onBack = { navController.popBackStack() },
            )
        }
    }
}

