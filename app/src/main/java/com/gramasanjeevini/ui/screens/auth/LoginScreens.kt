package com.gramasanjeevini.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gramasanjeevini.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLoginScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    val form by viewModel.form.collectAsState()
    val user by viewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(user) {
        if (user != null) onLoggedIn()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("User Login") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.email,
                onValueChange = viewModel::updateEmail,
                label = { Text("Email") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            Button(onClick = viewModel::loginUser, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistLoginScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    val form by viewModel.form.collectAsState()
    val user by viewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(user) {
        if (user != null) onLoggedIn()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pharmacist/Admin Login") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.email,
                onValueChange = viewModel::updateEmail,
                label = { Text("Work Email") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            Button(onClick = viewModel::loginPharmacist, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Note: pharmacist/admin role is set in Firestore.")
                Spacer(Modifier.width(1.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    val form by viewModel.form.collectAsState()
    val user by viewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(user) {
        if (user != null) onLoggedIn()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.displayName,
                onValueChange = viewModel::updateDisplayName,
                label = { Text("Full name") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.email,
                onValueChange = viewModel::updateEmail,
                label = { Text("Email") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Password (min 6 chars)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            Button(onClick = viewModel::registerUser, modifier = Modifier.fillMaxWidth()) {
                Text("Create account")
            }
        }
    }
}

