package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The one row container used by spending lists: a raised rounded card with a visible gap to its
 * neighbours. An optional [accent] paints a 4dp rail on the leading edge, using the category or
 * service colour so the list reads by identity rather than by text alone.
 */
@Composable
fun ListCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = shape,
            ),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        if (accent == null) {
            content()
        } else {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(accent),
                )
                Box(Modifier.weight(1f)) {
                    content()
                }
            }
        }
    }
}
