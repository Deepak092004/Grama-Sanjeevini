package com.gramasanjeevini.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gramasanjeevini.domain.models.UserRole
import com.gramasanjeevini.ui.components.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGateScreen(
    snackbarHostState: SnackbarHostState,
    role: UserRole,
    onOpenAdmin: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Admin") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (role == UserRole.PHARMACIST || role == UserRole.ADMIN) {
                Button(onClick = onOpenAdmin, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Stock Manager")
                }
            } else {
                ErrorState("You don't have access to Admin features.")
            }
        }
    }
}

