package com.danilkinkin.buckwheat.wallet

import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.toLocalDate
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun rememberExportCSV(
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
): () -> Unit {
    if (activityResultRegistryOwner === null) return {}

    var createHistoryFileLauncher: ManagedActivityResultLauncher<String, Uri?>? = null

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val startDate by remember { mutableStateOf(spendsViewModel.startDate.value) }
    val finishDate by remember { mutableStateOf(spendsViewModel.finishDate.value) }

    val snackBarExportToCSVSuccess = stringResource(R.string.export_to_csv_success)
    val snackBarExportToCSVFailed = stringResource(R.string.export_to_csv_failed)

    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    val from = if (yearFormatter.format(startDate!!.toLocalDate()) == yearFormatter.format(
            finishDate!!.toLocalDate()
        )
    ) {
        DateTimeFormatter.ofPattern("dd-MM").format(startDate!!.toLocalDate())
    } else {
        DateTimeFormatter.ofPattern("dd-MM-yyyy").format(startDate!!.toLocalDate())
    }
    val to = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(finishDate!!.toLocalDate())

    CompositionLocalProvider(
        LocalActivityResultRegistryOwner provides activityResultRegistryOwner
    ) {
        createHistoryFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv")
        ) {
            coroutineScope.launch {
                if (it !== null) {
                    spendsViewModel.exportAsCsv(context, it)
                    appViewModel.snackbarHostState.showSnackbar(snackBarExportToCSVSuccess)
                } else {
                    appViewModel.snackbarHostState.showSnackbar(snackBarExportToCSVFailed)
                }
            }
        }
    }

    return {
        createHistoryFileLauncher?.launch("spends (from $from to $to).csv")
    }
}
