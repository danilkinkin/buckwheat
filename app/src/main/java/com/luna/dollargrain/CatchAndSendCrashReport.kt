package com.luna.dollargrain

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.util.collectEnvInfo
import com.luna.dollargrain.util.sendEmail

@Composable
fun CatchAndSendCrashReport(
    appViewModel: AppViewModel = viewModel(),
) {
    val context = LocalContext.current
    val intent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ellipticobj/issues/new/")) }


    val errorForReport = LocalContext.current.errorForReport

    val crashReportReadyMessage = "An error occurred ;("
    val sendActionLabel = "Send report!!!"

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
                context.startActivity(intent)
            }
        }

        return@DisposableEffect onDispose { }
    }
}