package com.danilkinkin.buckwheat.editor.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.BigIconButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.editor.EditMode
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.home.MonthTotal
import com.danilkinkin.buckwheat.settings.SETTINGS_SHEET

@Composable
fun EditorToolbar(
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    val isDebug = appViewModel.isDebug.observeAsState(false)
    val mode by editorViewModel.mode.observeAsState(EditMode.ADD)

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isDebug.value) 6.dp else 20.dp, end = 6.dp, top = 6.dp)
            .statusBarsPadding(),
    ) {
        if (isDebug.value) {
            BigIconButton(
                icon = painterResource(R.drawable.ic_developer_mode),
                contentDescription = null,
                onClick = { appViewModel.openSheet(PathState(DEBUG_MENU_SHEET)) },
            )
        }
        if (mode == EditMode.EDIT) {
            Spacer(modifier = Modifier.width(4.dp))
            CancelEditSpent()
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonthTotal()
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        BigIconButton(
            icon = painterResource(R.drawable.ic_settings),
            contentDescription = null,
            onClick = {
                appViewModel.openSheet(PathState(SETTINGS_SHEET))
            },
        )
    }
}
