package com.danilkinkin.buckwheat.export

import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.errorForReport
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Exports the whole ledger as CSV.
 *
 * Deliberately independent of the budget: no start/finish period is needed, which is what the
 * old wallet-scoped version assumed. It lives outside `wallet/` so deleting the budget layer
 * cannot take the backup path with it.
 */
@Composable
fun rememberExportCSV(
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
): () -> Unit {
    if (activityResultRegistryOwner === null) return {}

    var createFileLauncher: ManagedActivityResultLauncher<String, Uri?>? = null

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val snackBarSuccess = stringResource(R.string.export_to_csv_success)
    val snackBarFailed = stringResource(R.string.export_to_csv_failed)

    // ISO-8601 keeps the file machine-readable for a future importer; the date in the name
    // sorts exports chronologically in a file manager.
    val fileName = "eva-${LocalDate.now()}.csv"
    val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    CompositionLocalProvider(
        LocalActivityResultRegistryOwner provides activityResultRegistryOwner
    ) {
        createFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            if (uri == null) {
                coroutineScope.launch { appViewModel.showSnackbar(snackBarFailed) }
                return@rememberLauncherForActivityResult
            }

            coroutineScope.launch {
                try {
                    val stream = context.contentResolver.openOutputStream(uri)
                    val printer = CSVPrinter(
                        stream?.writer(),
                        // Builder implements Supplier since commons-csv 1.13; build() is deprecated.
                        CSVFormat.Builder.create()
                            .setHeader("date", "amount", "comment")
                            .get()
                    )

                    spendsViewModel.spends.value.orEmpty().forEach { spent ->
                        printer.printRecord(
                            spent.date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .format(dateFormatter),
                            spent.value.toPlainString(),
                            spent.comment,
                        )
                    }

                    printer.flush()
                    printer.close()
                    stream?.close()

                    appViewModel.showSnackbar(snackBarSuccess)
                } catch (e: Exception) {
                    context.errorForReport = e.stackTraceToString()
                    appViewModel.showSnackbar(snackBarFailed)
                }
            }
        }
    }

    return {
        try {
            createFileLauncher?.launch(fileName)
        } catch (e: Exception) {
            context.errorForReport = e.stackTraceToString()
            appViewModel.showSnackbar(snackBarFailed)
        }
    }
}
