package com.danilkinkin.buckwheat.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.GradientCard
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toLocalDate
import com.danilkinkin.buckwheat.widget.extend.ExtendWidgetReceiver
import com.danilkinkin.buckwheat.widget.minimal.MinimalWidgetReceiver
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Date

const val SETTINGS_TRY_WIDGET_SHEET = "settings.tryWidget"

@Composable
fun TryWidget(appViewModel: AppViewModel = hiltViewModel(), onTried: () -> Unit) {

    Box(contentAlignment = Alignment.TopStart) {
        ButtonRow(
            icon = painterResource(R.drawable.ic_widgets),
            text = stringResource(R.string.home_widgets_label),
            onClick = {
                appViewModel.openSheet(PathState(SETTINGS_TRY_WIDGET_SHEET))
                onTried()
            },
        )
    }

}

@Composable
fun TryWidgetDialog(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val subscriptions by subscriptionsViewModel.subscriptions.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())

    val today = Date()
    val currentMonth = YearMonth.now()
    val todayTotal = spends
        .filter { isSameDay(it.date, today) }
        .fold(BigDecimal.ZERO) { total, spend -> total + spend.value }
    val monthTotal = spends
        .filter { YearMonth.from(it.date.toLocalDate()) == currentMonth }
        .fold(BigDecimal.ZERO) { total, spend -> total + spend.value }
    val next = subscriptions
        .filter { it.active }
        .minByOrNull { it.nextDue.time }

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_widgets_label),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WidgetRow(
                    description = stringResource(R.string.app_widget_minimal_name),
                    onClick = {
                        appWidgetManager.requestPinAppWidget(
                            ComponentName(context, MinimalWidgetReceiver::class.java),
                            null,
                            null
                        )
                    },
                ) {
                    MinimalWidgetPreview(
                        todayTotal = todayTotal,
                        currency = currency,
                    )
                }

                WidgetRow(
                    description = stringResource(R.string.app_widget_extend_name),
                    onClick = {
                        appWidgetManager.requestPinAppWidget(
                            ComponentName(context, ExtendWidgetReceiver::class.java),
                            null,
                            null
                        )
                    },
                ) {
                    ExtendWidgetPreview(
                        todayTotal = todayTotal,
                        monthTotal = monthTotal,
                        next = next,
                        currency = currency,
                    )
                }
            }

            Text(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.try_home_widgets_desc),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
                    .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
            )
        }
    }
}

@Composable
private fun WidgetRow(
    description: String,
    onClick: () -> Unit,
    preview: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        preview()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center,
            text = description,
            style = MaterialTheme.typography.bodyMedium
                .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
        )
    }
}

@Composable
private fun MinimalWidgetPreview(
    todayTotal: BigDecimal,
    currency: ExtendCurrency,
) {
    GradientCard(
        modifier = Modifier
            .width(260.dp)
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.today),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                )
                Text(
                    text = numberFormat(LocalContext.current, todayTotal, currency),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun ExtendWidgetPreview(
    todayTotal: BigDecimal,
    monthTotal: BigDecimal,
    next: Subscription?,
    currency: ExtendCurrency,
) {
    GradientCard(
        modifier = Modifier
            .width(260.dp)
            .height(164.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(164.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.today),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                )
                Text(
                    text = numberFormat(LocalContext.current, todayTotal, currency),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${stringResource(R.string.this_month)}  " +
                            numberFormat(LocalContext.current, monthTotal, currency),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
                if (next != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Next · ${next.name} · " +
                                prettyDate(next.nextDue, pattern = "d MMM"),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(24.dp),
            )
        }
    }
}
