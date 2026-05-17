package com.gramasanjeevini.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gramasanjeevini.ui.components.EmergencyBadge
import com.gramasanjeevini.ui.components.ErrorState
import com.gramasanjeevini.ui.components.LoadingBlock
import com.gramasanjeevini.ui.state.UiState
import com.gramasanjeevini.ui.viewmodel.AuthViewModel
import com.gramasanjeevini.ui.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: SplashViewModel,
    authViewModel: AuthViewModel,
    onGoToAuth: () -> Unit,
    onGoToMain: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val user by authViewModel.user.collectAsState()

    val scale = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splashScale",
    )

    LaunchedEffect(state) {
        if (state is UiState.Data) {
            if (user == null) onGoToAuth() else onGoToMain()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val s = state) {
                UiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .scale(scale.value),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Grama-Sanjeevini", fontWeight = FontWeight.SemiBold)
                        EmergencyBadge(modifier = Modifier.padding(top = 10.dp))
                        LoadingBlock(text = "Starting up...")
                    }
                }
                is UiState.Error -> ErrorState(message = s.message)
                is UiState.Data -> Text("Ready") // Usually never visible (auto-navigates).
            }
        }
    }
}

