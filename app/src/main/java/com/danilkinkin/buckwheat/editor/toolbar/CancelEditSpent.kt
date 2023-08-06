package com.danilkinkin.buckwheat.editor.toolbar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.SpendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.CancelEditSpent(
    spendsViewModel: SpendsViewModel = hiltViewModel()
) {
    Card(
        modifier = Modifier
            .weight(1F)
            .padding(0.dp, 6.dp)
            .height(44.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        onClick = {
            spendsViewModel.resetSpent()
        }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(14.dp))
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.cancel_editing),
                style = MaterialTheme.typography.labelLarge,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
    }
}