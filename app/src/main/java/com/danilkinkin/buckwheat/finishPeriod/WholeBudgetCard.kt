package com.danilkinkin.buckwheat.finishPeriod

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Composable
fun WholeBudgetCard(
    budget: BigDecimal,
    currency: ExtendCurrency,
    startDate: Date,
    finishDate: Date,
) {
    StatCard(
        modifier = Modifier.fillMaxWidth(),
        value = prettyCandyCanes(
            budget,
            currency = currency,
        ),
        label = stringResource(R.string.whole_budget),
        valueFontSize = MaterialTheme.typography.displaySmall.fontSize,
        content = {
            val textColor = LocalContentColor.current

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = prettyDate(
                            startDate,
                            showTime = false,
                            forceShowDate = true,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    )
                    Text(
                        text = stringResource(R.string.label_start_date),
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.6f),
                    )
                }

                Spacer(
                    Modifier
                        .width(16.dp)
                )

                Arrow(
                    modifier = Modifier.height(24.dp).widthIn(24.dp).weight(1f),
                )

                Spacer(
                    Modifier
                        .width(16.dp)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = prettyDate(
                            finishDate,
                            showTime = false,
                            forceShowDate = true,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    )
                    Text(
                        text = stringResource(R.string.label_finish_date),
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    )
}

@Composable
fun Arrow(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier) {


        val width = this.size.width
        val height = this.size.height
        val heightHalf = height / 2

        val thickness = 6
        val thicknessHalf = thickness / 2

        val trianglePath = Path().let {
            it.moveTo(11f, heightHalf - thicknessHalf)
            it.lineTo(width - 22.4f, heightHalf - thicknessHalf)
            it.lineTo(width - 37.4f, heightHalf - 18)
            it.lineTo(width - 33, heightHalf - 22.4f)
            it.lineTo(width - 10.5f, heightHalf)
            it.lineTo(width - 33, heightHalf + 22.4f)
            it.lineTo(width - 37.4f, heightHalf + 18)
            it.lineTo(width - 22.4f, heightHalf + thicknessHalf)
            it.lineTo(width - 22.4f, heightHalf + thicknessHalf)
            it.lineTo(11f, heightHalf + thicknessHalf)

            it.close()

            it
        }

        drawPath(
            path = trianglePath,
            SolidColor(tint),
            style = Fill
        )
    }
}

@Preview
@Composable
private fun PreviewChart() {
    BuckwheatTheme {
        Box() {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                tint = Color.Green,
                contentDescription = null,
            )
            Arrow(
                modifier = Modifier.height(24.dp).width(100.dp),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        WholeBudgetCard(
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
            startDate = LocalDate.now().minusDays(28).toDate(),
            finishDate = Date(),
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        WholeBudgetCard(
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
            startDate = LocalDate.now().minusDays(28).toDate(),
            finishDate = Date(),
        )
    }
}