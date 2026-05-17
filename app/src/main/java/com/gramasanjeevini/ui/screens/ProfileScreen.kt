package com.gramasanjeevini.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gramasanjeevini.domain.models.UserRole
import com.gramasanjeevini.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    snackbarHostState: SnackbarHostState,
    authViewModel: AuthViewModel,
    role: UserRole,
    onOpenExpiryWatch: () -> Unit,
) {
    val user by authViewModel.user.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Profile") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = user?.displayName?.ifBlank { "User" } ?: "User", style = MaterialTheme.typography.titleLarge)
            Text(text = user?.email ?: "No email", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Role: ${role.name}", style = MaterialTheme.typography.bodyLarge)

            if (role == UserRole.PHARMACIST || role == UserRole.ADMIN) {
                Button(onClick = onOpenExpiryWatch, modifier = Modifier.fillMaxWidth()) {
                    Text("Expiry Watch")
                }
            }

            Button(onClick = authViewModel::logout, modifier = Modifier.fillMaxWidth()) {
                Text("Logout")
            }
        }
    }
}

