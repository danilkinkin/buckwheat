package com.luna.dollargrain.editor.toolbar.restBudgetPill

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.dollargrain.R
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.util.HarmonizedColorPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusLabel(
    harmonizedColor: HarmonizedColorPalette,
    appViewModel: AppViewModel = hiltViewModel(),
    restBudgetPillViewModel: RestBudgetPillViewModel = hiltViewModel(),
) {
    val budgetState by restBudgetPillViewModel.state.observeAsState(DaileBudgetState.NORMAL)

    val textColor = LocalContentColor.current

    Box(contentAlignment = Alignment.CenterStart) {
        Row(
            modifier = Modifier.height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val textStartOffset by animateDpAsState(
                label = "textStartOffset",
                targetValue = if (budgetState === DaileBudgetState.OVERDRAFT || budgetState === DaileBudgetState.BUDGET_END) 44.dp else 18.dp,
                animationSpec = TweenSpec(250),
            )

            Spacer(modifier = Modifier.width(textStartOffset))
            Text(
                text = when (budgetState) {
                    DaileBudgetState.NORMAL, DaileBudgetState.NOT_SET, null -> stringResource(R.string.rest_budget_for_today)
                    DaileBudgetState.OVERDRAFT -> stringResource(R.string.new_daily_budget_short)
                    DaileBudgetState.BUDGET_END -> stringResource(R.string.budget_end)
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                ),
                color = textColor.copy(alpha = 0.6f),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
        AnimatedVisibility(
            visible = budgetState === DaileBudgetState.OVERDRAFT || budgetState === DaileBudgetState.BUDGET_END,
            enter = fadeIn(tween(durationMillis = 250)),
            exit = fadeOut(tween(durationMillis = 250)),
        ) {
            Card(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = harmonizedColor.container.copy(alpha = 0f),
                    contentColor = harmonizedColor.onContainer,
                ),
                onClick = {
                    if (budgetState === DaileBudgetState.BUDGET_END) {
                        appViewModel.openSheet(
                            PathState(
                                BUDGET_IS_OVER_DESCRIPTION_SHEET
                            )
                        )
                    } else {
                        appViewModel.openSheet(
                            PathState(
                                NEW_DAY_BUDGET_DESCRIPTION_SHEET
                            )
                        )
                    }
                }
            ) {
                Row(
                    Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(14.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                }
            }
        }
    }
}