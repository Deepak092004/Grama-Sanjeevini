package com.gramasanjeevini.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EmergencyBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFB3261E),
        contentColor = Color.White,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            text = "EMERGENCY",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
fun StockChip(inStock: Boolean) {
    val (label, colors) = if (inStock) {
        "In Stock" to AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFFE7F5EC),
            labelColor = Color(0xFF1B5E20),
        )
    } else {
        "Out of Stock" to AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFFFFEDEA),
            labelColor = Color(0xFF7F1D1D),
        )
    }
    AssistChip(
        onClick = { /* read-only */ },
        label = { Text(label) },
        colors = colors,
    )
}

@Composable
fun LoadingBlock(text: String = "Loading...") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ErrorState(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFFFFEDEA),
        contentColor = Color(0xFF7F1D1D),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Something went wrong", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun AnimatedTopHint(visible: Boolean, text: String) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

