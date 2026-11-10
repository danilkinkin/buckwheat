package com.danilkinkin.buckwheat.subscriptions

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R

/**
 * Small renewal cue. Hidden when the due date is more than a week away so the list stays calm.
 */
@Composable
fun DueStatusChip(
    daysUntilDue: Long,
    modifier: Modifier = Modifier,
) {
    if (daysUntilDue > 7) return

    val overdue = daysUntilDue <= 0

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (overdue) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        },
        contentColor = if (overdue) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onTertiaryContainer
        },
    ) {
        Text(
            text = if (overdue) {
                stringResource(R.string.due_now)
            } else {
                stringResource(R.string.due_in_days, daysUntilDue)
            },
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
