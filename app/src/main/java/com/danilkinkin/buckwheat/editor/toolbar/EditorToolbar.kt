package com.danilkinkin.buckwheat.editor.toolbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.BigIconButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.settings.SETTINGS_SHEET
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.wallet.WALLET_SHEET
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorToolbar(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onOpenHistory: (() -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val isDebug = appViewModel.isDebug.observeAsState(false)

    val lastDaySpends by spendsViewModel.getCountLastDaySpends().observeAsState(0)
    val mode by spendsViewModel.mode.observeAsState(SpendsViewModel.Mode.ADD)

    val spendsCountScale = remember { Animatable(1f) }

    observeLiveData(spendsViewModel.stage) {
        if (it === SpendsViewModel.Stage.COMMITTING_SPENT) {
            coroutineScope.launch {
                spendsCountScale.animateTo(
                    1.05f,
                    animationSpec = tween(
                        durationMillis = 20,
                        easing = LinearEasing
                    )
                )
                spendsCountScale.animateTo(
                    1f,
                    animationSpec = tween(
                        durationMillis = 120,
                        easing = LinearEasing,
                    )
                )
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp)
            .statusBarsPadding(),
    ) {

        if (mode === SpendsViewModel.Mode.ADD) {
            if (lastDaySpends != 0) {
                Box(Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .scale(spendsCountScale.value)
                            .clip(CircleShape)
                            .then(
                                if (onOpenHistory !== null) Modifier.clickable { onOpenHistory() }
                                else Modifier
                            )

                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                            text = String.format(
                                pluralStringResource(R.plurals.spends_today, count = lastDaySpends),
                                lastDaySpends,
                            ),
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
            }
        } else if (mode === SpendsViewModel.Mode.EDIT) {
            Box(Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .scale(spendsCountScale.value)
                        .clip(CircleShape)
                        .clickable { spendsViewModel.resetSpent() }
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                        text = stringResource(R.string.cancel_editing),
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        if (isDebug.value) {
            BigIconButton(
                icon = painterResource(R.drawable.ic_developer_mode),
                contentDescription = null,
                onClick = { appViewModel.openSheet(PathState(DEBUG_MENU_SHEET)) },
            )
        }
        BigIconButton(
            icon = painterResource(R.drawable.ic_balance_wallet),
            contentDescription = null,
            onClick = { appViewModel.openSheet(PathState(WALLET_SHEET)) },
        )
        BigIconButton(
            icon = painterResource(R.drawable.ic_settings),
            contentDescription = null,
            onClick = { appViewModel.openSheet(PathState(SETTINGS_SHEET)) },
        )
    }
}