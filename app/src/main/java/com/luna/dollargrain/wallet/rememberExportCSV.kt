package com.luna.dollargrain.wallet

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
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.errorForReport
import com.luna.dollargrain.util.toLocalDate
import com.luna.dollargrain.util.toLocalDateTime
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

    val startPeriodDate by remember {
        mutableStateOf(spendsViewModel.startPeriodDate.value?.toLocalDate())
    }
    val finishPeriodDate by remember {
        mutableStateOf(spendsViewModel.finishPeriodDate.value?.let {
            LocalDate.now().coerceAtMost(it.toLocalDate())
        })
    }

    val snackBarExportToCSVSuccess = stringResource(R.string.export_to_csv_success)
    val snackBarExportToCSVFailed = stringResource(R.string.export_to_csv_failed)

    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    val from = if (
        yearFormatter.format(startPeriodDate) == yearFormatter.format(finishPeriodDate)
    ) {
        DateTimeFormatter.ofPattern("dd-MM").format(startPeriodDate)
    } else {
        DateTimeFormatter.ofPattern("dd-MM-yyyy").format(startPeriodDate)
    }
    val to = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(finishPeriodDate)


    val fileName = stringResource(R.string.export_to_csv_file_name, from, to)

    CompositionLocalProvider(
        LocalActivityResultRegistryOwner provides activityResultRegistryOwner
    ) {
        createHistoryFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            if (uri === null) {
                coroutineScope.launch {
                    appViewModel.showSnackbar(snackBarExportToCSVFailed)
                }

                return@rememberLauncherForActivityResult
            }

            coroutineScope.launch {
                val stream = context.contentResolver.openOutputStream(uri)

                val printer = CSVPrinter(
                    stream?.writer(),
                    CSVFormat.Builder.create().setHeader("amount", "comment", "commit_time")
                        .build()
                )
                val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

                spendsViewModel.spends.value!!.forEach { spent ->
                    printer.printRecord(
                        spent.value,
                        spent.comment,
                        spent.date.toLocalDateTime().format(dateFormatter),
                    )
                }

                printer.flush()
                printer.close()
                stream?.close()

                appViewModel.showSnackbar(snackBarExportToCSVSuccess)
            }
        }
    }

    return {
        try {
            createHistoryFileLauncher?.launch("$fileName.csv")
        } catch (e: Exception) {
            context.errorForReport = e.stackTraceToString()

            appViewModel.showSnackbar(snackBarExportToCSVFailed)
        }
    }
}
