package com.luna.dollargrain.editor.toolbar

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.editor.EditMode
import com.luna.dollargrain.editor.EditorViewModel
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.CancelEditSpent(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    val mode by editorViewModel.mode.observeAsState(EditMode.ADD)

    val coroutineScope = rememberCoroutineScope()
    var predictiveBackProgress by remember {
        mutableFloatStateOf(0f)
    }


    Card(
        modifier = Modifier
            .weight(1F)
            .padding(0.dp, 5.dp)
            .height(46.dp)
            .scale(1f + predictiveBackProgress * 0.08f),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        onClick = {
            editorViewModel.resetEditingSpent()
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 10.dp * (1f - predictiveBackProgress),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),

                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = predictiveBackProgress * 0.2f),
            ) {}

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
                    text = "cancel editing",
                    style = MaterialTheme.typography.labelLarge,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
                Spacer(modifier = Modifier.width(14.dp))
            }
        }
    }

    PredictiveBackHandler(mode == EditMode.EDIT) { progress ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
            }

            coroutineScope.launch {
                editorViewModel.resetEditingSpent()
                predictiveBackProgress = 0f
            }
        } catch (e: CancellationException) {
            predictiveBackProgress = 0f
        }
    }
}