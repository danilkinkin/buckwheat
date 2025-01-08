package com.luna.dollargrain.base

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun Divider() {
    androidx.compose.material3.Divider(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    )
}