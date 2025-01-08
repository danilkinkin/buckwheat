package com.luna.dollargrain

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.dollargrain.R
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.util.collectEnvInfo
import com.luna.dollargrain.util.sendEmail

@Composable
fun CatchAndSendCrashReport(
    appViewModel: AppViewModel = viewModel(),
) {
    val context = LocalContext.current
    val errorForReport = LocalContext.current.errorForReport

    val crashReportReadyMessage = stringResource(R.string.crash_report_ready)
    val sendActionLabel = stringResource(R.string.send_crash_report)
    val addYourCommentToReportHint = stringResource(R.string.add_your_comment_to_report)

    DisposableEffect(key1 = errorForReport) {
        if (errorForReport.isNullOrEmpty()) {
            return@DisposableEffect onDispose { }
        }

        appViewModel.showSnackbar(
            message = crashReportReadyMessage,
            actionLabel = sendActionLabel,
            duration = SnackbarDuration.Long,
        ) { snackbarResult ->
            if (snackbarResult == SnackbarResult.ActionPerformed) {
                sendEmail(
                    context,
                    arrayOf("luna@hackclub.app"),
                    "dollargrain bug report",
                    """
    
    
$addYourCommentToReportHint


${collectEnvInfo(context)}
---- Error info ----------------------------
$errorForReport
""".trimIndent(),
                )

                context.errorForReport = null
            }
        }

        onDispose { }
    }
}