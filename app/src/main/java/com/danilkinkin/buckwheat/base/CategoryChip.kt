package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R

@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    iconKey: String? = null,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    // In dark themes a variant surface sits almost on top of the popup card; an outline keeps
    // the unselected pills legible instead of dissolving into it.
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)

    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) androidx.compose.ui.graphics.Color.Transparent else outlineColor,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconKey != null) {
                Icon(
                    painter = painterResource(
                        categoryIconRes(iconKey) ?: R.drawable.ic_cat_other
                    ),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}
