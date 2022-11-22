package com.danilkinkin.buckwheat.settings

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.stringPreferencesKey
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.dataStore
import com.danilkinkin.buckwheat.util.switchIsSendCrashReport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun SendingCrashReport() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val sendCrashReport = remember {
        val currentValue = runBlocking { context.dataStore.data.first() }

        val isSend = (currentValue[stringPreferencesKey("send_crash_report")] ?: "true").toBoolean()

        mutableStateOf(isSend)
    }

    fun handleSwitchCrashReportDisable(enabled: Boolean) {
        sendCrashReport.value = enabled
        coroutineScope.launch {
            switchIsSendCrashReport(context, enabled)
        }
    }

    ButtonRow(
        icon = painterResource(R.drawable.ic_bug_report),
        text = stringResource(R.string.not_send_crash_report),
        description = stringResource(R.string.not_send_crash_report_description),
        wrapMainText = true,
        onClick = {
            handleSwitchCrashReportDisable(!sendCrashReport.value)
        },
        endContent = {
            Switch(
                checked = sendCrashReport.value,
                onCheckedChange = {
                    handleSwitchCrashReportDisable(!sendCrashReport.value)
                },
            )

        }
    )
}