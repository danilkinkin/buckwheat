package com.danilkinkin.buckwheat.editor.restBudget

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BudgetEndWarn(
    modifier: Modifier = Modifier,
    endBudget: Boolean,
    budgetPerDaySplit: String,
    onClick: () -> Unit = {},
) {

    val containerColor by animateColorAsState(
        targetValue = if (endBudget) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 250)
    )
    val contentColor by animateColorAsState(
        targetValue = if (endBudget) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 250)
    )
    val borderColor by animateColorAsState(
        targetValue = if (endBudget) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 250)
    )


    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        shape = CircleShape,
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape,
            ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    top = 12.dp,
                    end = 20.dp,
                    bottom = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedContent(
                targetState = endBudget,
                transitionSpec = {
                    if (targetState && !initialState) {
                        slideInVertically(
                            tween(durationMillis = 250)
                        ) { height -> height } + fadeIn(
                            tween(durationMillis = 250)
                        ) with slideOutVertically(
                            tween(durationMillis = 250)
                        ) { height -> -height } + fadeOut(
                            tween(durationMillis = 250)
                        )
                    } else {
                        slideInVertically(
                            tween(durationMillis = 250)
                        ) { height -> -height } + fadeIn(
                            tween(durationMillis = 250)
                        ) with slideOutVertically(
                            tween(durationMillis = 250)
                        ) { height -> height } + fadeOut(
                            tween(durationMillis = 250)
                        )
                    }.using(
                        SizeTransform(clip = false)
                    )
                }
            ) { targetEndBudget ->
                if (targetEndBudget) {
                    Box(
                        modifier = Modifier.heightIn(24.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = stringResource(R.string.budget_end),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                } else {
                    TextWithLabel(
                        value = budgetPerDaySplit,
                        label = stringResource(R.string.new_daily_budget),
                        fontSizeValue = MaterialTheme.typography.bodyLarge.fontSize,
                        fontSizeLabel = MaterialTheme.typography.labelMedium.fontSize,
                        contentPaddingValues = PaddingValues(0.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        BudgetEndWarn(
            endBudget = false,
            budgetPerDaySplit = "300$",
        )
    }
}