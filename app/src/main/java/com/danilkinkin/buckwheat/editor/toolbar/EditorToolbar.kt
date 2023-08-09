package com.danilkinkin.buckwheat.editor.toolbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.BigIconButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.EditMode
import com.danilkinkin.buckwheat.data.EditStage
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill.RestBudgetPill
import com.danilkinkin.buckwheat.settings.SETTINGS_SHEET
import com.danilkinkin.buckwheat.util.observeLiveData
import kotlinx.coroutines.launch

@Composable
fun EditorToolbar(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val isDebug = appViewModel.isDebug.observeAsState(false)
    val mode by spendsViewModel.mode.observeAsState(EditMode.ADD)
    val showSettingsDot = remember {
        mutableStateOf(appViewModel.getBooleanValue("previewWidgets", true))
    }

    val spendsCountScale = remember { Animatable(1f) }

    observeLiveData(spendsViewModel.stage) {
        if (it === EditStage.COMMITTING_SPENT) {
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
            .padding(start = if (isDebug.value) 6.dp else 20.dp, end = 6.dp, top = 12.dp)
            .statusBarsPadding(),
    ) {
        if (isDebug.value) {
            BigIconButton(
                icon = painterResource(R.drawable.ic_developer_mode),
                contentDescription = null,
                onClick = { appViewModel.openSheet(PathState(DEBUG_MENU_SHEET)) },
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        if (mode == EditMode.EDIT) {
            CancelEditSpent()
        } else {
            RestBudgetPill()
        }
        Spacer(modifier = Modifier.width(4.dp))
        Box(contentAlignment = Alignment.TopEnd) {
            BigIconButton(
                icon = painterResource(R.drawable.ic_settings),
                contentDescription = null,
                onClick = {
                    appViewModel.openSheet(PathState(SETTINGS_SHEET, callback = {
                        showSettingsDot.value = appViewModel.getBooleanValue("previewWidgets", true)
                    }))
                },
            )
            if (showSettingsDot.value) {
                Box(
                    modifier = Modifier
                        .offset((-10).dp, 10.dp)
                        .background(MaterialTheme.colors.error, shape = CircleShape)
                        .size(8.dp)
                )
            }
        }
    }
}
