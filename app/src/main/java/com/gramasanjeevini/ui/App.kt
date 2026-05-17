package com.gramasanjeevini.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.gramasanjeevini.ui.navigation.AppNavHost

@Composable
fun App() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    AppNavHost(
        navController = navController,
        snackbarHostState = snackbarHostState,
    )
}

