package com.danilkinkin.buckwheat.spendsHistory

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.base.Collapse
import com.danilkinkin.buckwheat.base.DeleteState
import com.danilkinkin.buckwheat.base.SwipeForDismiss
import com.danilkinkin.buckwheat.base.rememberSwipeForDismiss
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Spent(
    spent: Spent,
    currency: ExtendCurrency,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val swipeableState = rememberSwipeForDismiss()

    Collapse(
        show = !spent.deleted,
        onHide = {
            coroutineScope.launch {
                swipeableState.animateTo(DeleteState.IDLE, anim = TweenSpec(0))
            }
        },
    ) {
        SwipeForDismiss(
            modifier = Modifier.fillMaxWidth(),
            swipeableState = swipeableState,
            contentColor = colorEditor,
            onSwiped = {
                onDelete()
            }
        ) {
            Row(
                modifier = modifier.fillMaxWidth()
            ) {
                Text(
                    text = prettyCandyCanes(spent.value, currency = currency),
                    style = MaterialTheme.typography.displaySmall,
                    color = colorOnEditor,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                )
                Spacer(Modifier.weight(1F))
                Box(Modifier) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 32.dp, vertical = 16.dp),
                        text = prettyDate(spent.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorOnEditor,
                    )
                }
            }
        }
    }


}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        Spent(
            Spent(value = BigDecimal(12340), date = Date()),
            ExtendCurrency(type = CurrencyType.NONE)
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        Spent(
            Spent(value = BigDecimal(12340), date = Date()),
            ExtendCurrency(type = CurrencyType.NONE)
        )
    }
}