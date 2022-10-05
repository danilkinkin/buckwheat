package com.danilkinkin.buckwheat.editor

import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.BigIconButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.join
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import com.danilkinkin.buckwheat.util.tryConvertStringToNumber
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min

enum class AnimState { FIRST_IDLE, EDITING, COMMIT, IDLE, RESET }

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun Editor(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onOpenWallet: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onDebugMenu: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
) {
    val isDebug = appViewModel.isDebug.observeAsState(false)

    val lastDaySpends by spendsViewModel.getCountLastDaySpends().observeAsState(0)

    var currState by remember { mutableStateOf<AnimState?>(null) }
    var currAnimator by remember { mutableStateOf<ValueAnimator?>(null) }

    val localDensity = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var budgetValue by remember { mutableStateOf("") }
    var restBudgetValue by remember { mutableStateOf("") }
    var spentValue by remember { mutableStateOf("0") }
    var budgetPerDaySplit by remember { mutableStateOf("") }
    var overdaft by remember { mutableStateOf(false) }
    var endBudget by remember { mutableStateOf(false) }

    var budgetHeight by remember { mutableStateOf(0F) }
    var restBudgetHeight by remember { mutableStateOf(0F) }
    var spentHeight by remember { mutableStateOf(0F) }

    var budgetOffset by remember { mutableStateOf(0F) }
    var restBudgetOffset by remember { mutableStateOf(0F) }
    var spentOffset by remember { mutableStateOf(0F) }

    var budgetAlpha by remember { mutableStateOf(0F) }
    var restBudgetAlpha by remember { mutableStateOf(0F) }
    var spentAlpha by remember { mutableStateOf(0F) }

    var budgetValueFontSize by remember { mutableStateOf(60.sp) }
    var budgetLabelFontSize by remember { mutableStateOf(60.sp) }
    var restBudgetValueFontSize by remember { mutableStateOf(60.sp) }
    var restBudgetLabelFontSize by remember { mutableStateOf(60.sp) }
    var spentValueFontSize by remember { mutableStateOf(60.sp) }
    var spentLabelFontSize by remember { mutableStateOf(60.sp) }

    val spendsCountScale = remember { Animatable(1f) }

    fun calculateValues(
        budget: Boolean = true,
        restBudget: Boolean = true,
        spent: Boolean = true
    ) {
        val spentFromDailyBudget = spendsViewModel.spentFromDailyBudget.value!!
        val dailyBudget = spendsViewModel.dailyBudget.value!!

        if (budget) budgetValue = prettyCandyCanes(
            (dailyBudget - spentFromDailyBudget).coerceAtLeast(
                BigDecimal(0)
            ),
            currency = spendsViewModel.currency,
        )

        if (restBudget) {
            val newBudget = dailyBudget - spentFromDailyBudget - spendsViewModel.currentSpent

            overdaft = newBudget < BigDecimal(0)

            restBudgetValue =
                prettyCandyCanes(
                    newBudget.coerceAtLeast(BigDecimal(0)),
                    currency = spendsViewModel.currency,
                )

            val newPerDayBudget = spendsViewModel.calcBudgetPerDaySplit(
                applyCurrentSpent = true,
                excludeCurrentDay = true,
            )

            endBudget = newPerDayBudget < BigDecimal(0)

            budgetPerDaySplit = prettyCandyCanes(
                newPerDayBudget.coerceAtLeast(BigDecimal(0)),
                currency = spendsViewModel.currency,
            )
        }

        if (spent) {
            spentValue = spendsViewModel.rawSpentValue.value!!
        }
    }

    fun animFrame(state: AnimState, progress: Float = 1F) {
        when (state) {
            AnimState.FIRST_IDLE -> {
                budgetValueFontSize = 80.sp
                budgetLabelFontSize = 20.sp
                budgetOffset = 60.dp.value * (1F - progress)
                budgetAlpha = progress
            }
            AnimState.EDITING -> {
                var offset = 0F

                restBudgetValueFontSize = 30.sp
                restBudgetLabelFontSize = 16.sp
                offset += restBudgetHeight
                restBudgetOffset = (offset + spentHeight) * (1F - progress)
                restBudgetAlpha = 1F

                spentValueFontSize = 80.sp
                spentLabelFontSize = 20.sp
                spentOffset = (spentHeight + offset) * (1F - progress) - offset
                spentAlpha = 1F

                offset += spentHeight

                budgetValueFontSize = (80 - 60 * progress).sp
                budgetLabelFontSize = (20 - 10 * progress).sp
                budgetOffset = -offset * progress
                budgetAlpha = 1F
            }
            AnimState.COMMIT -> {
                var offset = 0F

                val progressA = min(progress * 2F, 1F)
                val progressB = max((progress - 0.5F) * 2F, 0F)

                restBudgetValueFontSize = (30 + 50 * progress).sp
                restBudgetLabelFontSize = (16 + 4 * progress).sp
                offset += restBudgetHeight
                restBudgetAlpha = 1F

                spentValueFontSize = 80.sp
                spentLabelFontSize = 20.sp
                spentOffset = -offset - 50 * progressB
                spentAlpha = 1F - progressB
                offset += spentHeight

                budgetValueFontSize = 20.sp
                budgetLabelFontSize = 10.sp
                budgetOffset = -offset - 50 * progressA
                budgetAlpha = 1F - progressA
            }
            AnimState.RESET -> {
                var offset = 0F

                restBudgetValueFontSize = 30.sp
                restBudgetLabelFontSize = 16.sp
                offset += restBudgetHeight
                restBudgetOffset = (offset + spentHeight) * progress

                spentValueFontSize = 80.sp
                spentLabelFontSize = 20.sp
                spentOffset = (spentHeight + offset) * progress - offset
                offset += spentHeight

                budgetValueFontSize = (20 + 60 * progress).sp
                budgetLabelFontSize = (10 + 10 * progress).sp
                budgetOffset = -offset * (1F - progress)
            }
            AnimState.IDLE -> {
                calculateValues(restBudget = false)

                budgetValueFontSize = 80.sp
                budgetLabelFontSize = 20.sp
                budgetOffset = 0F
                budgetAlpha = 1F

                restBudgetAlpha = 0F
            }
        }
    }

    fun animTo(state: AnimState) {
        if (currState == state) return

        currState = state

        if (currAnimator !== null) {
            currAnimator!!.pause()
        }

        currAnimator = ValueAnimator.ofFloat(0F, 1F)

        currAnimator!!.apply {
            duration = 220
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Float

                animFrame(state, animatedValue)
            }

            doOnEnd {
                if (state === AnimState.COMMIT) {
                    animFrame(AnimState.IDLE)
                }
            }

            start()
        }
    }

    observeLiveData(spendsViewModel.dailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.spentFromDailyBudget) {
        calculateValues(budget = currState !== AnimState.EDITING, restBudget = false)
    }

    observeLiveData(spendsViewModel.stage) {
        when (it) {
            SpendsViewModel.Stage.IDLE, null -> {
                if (currState === AnimState.EDITING) animTo(AnimState.RESET)
            }
            SpendsViewModel.Stage.CREATING_SPENT -> {
                calculateValues(budget = false)

                animTo(AnimState.EDITING)
            }
            SpendsViewModel.Stage.EDIT_SPENT -> {
                calculateValues(budget = false)

                animTo(AnimState.EDITING)
            }
            SpendsViewModel.Stage.COMMITTING_SPENT -> {
                animTo(AnimState.COMMIT)

                coroutineScope.launch {
                    spendsCountScale.animateTo(
                        1.05f,
                        animationSpec = tween(
                            durationMillis = 20,
                            easing = LinearEasing
                        )
                    )
                    spendsCountScale.animateTo(
                        1f,
                        animationSpec = tween(
                            durationMillis = 120,
                            easing = LinearEasing,
                        )
                    )
                }

                spendsViewModel.resetSpent()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp)
                .statusBarsPadding(),
        ) {
            if (lastDaySpends != 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .scale(spendsCountScale.value)
                        .clip(CircleShape)
                        .clickable { onOpenHistory() }
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                        text = String.format(
                            pluralStringResource(R.plurals.spends_today, count = lastDaySpends),
                            lastDaySpends,
                        ),
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            if (isDebug.value) {
                BigIconButton(
                    icon = painterResource(R.drawable.ic_developer_mode),
                    contentDescription = null,
                    onClick = onDebugMenu,
                )
            }
            BigIconButton(
                icon = painterResource(R.drawable.ic_balance_wallet),
                contentDescription = null,
                onClick = onOpenWallet,
            )
            BigIconButton(
                icon = painterResource(R.drawable.ic_settings),
                contentDescription = null,
                onClick = onOpenSettings,
            )
        }
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    if (currState === null) animTo(AnimState.FIRST_IDLE)
                },
        ) {
            TextWithLabel(
                value = budgetValue,
                label = stringResource(id = R.string.budget_for_today),
                fontSizeValue = budgetValueFontSize,
                fontSizeLabel = budgetLabelFontSize,
                modifier = Modifier
                    .offset(y = with(localDensity) { budgetOffset.toDp() })
                    .alpha(budgetAlpha)
                    .onGloballyPositioned {
                        budgetHeight = it.size.height.toFloat()
                    }
                    .padding(bottom = 24.dp)
            )
            EditableTextWithLabel(
                value = spentValue,
                label = stringResource(id = R.string.spent),
                onChangeValue = {
                    Log.d("Editor", "onChangeValue = $it")
                    val converted = tryConvertStringToNumber(it)

                    spendsViewModel.rawSpentValue.value = it

                    spendsViewModel.editSpent(converted.join().toBigDecimal())

                    if (it === "") {
                        runBlocking {
                            spendsViewModel.resetSpent()
                        }
                    }
                },
                currency = spendsViewModel.currency,
                fontSizeValue = spentValueFontSize,
                fontSizeLabel = spentLabelFontSize,
                modifier = Modifier
                    .offset(y = with(localDensity) { spentOffset.toDp() })
                    .alpha(spentAlpha)
                    .onGloballyPositioned {
                        spentHeight = it.size.height.toFloat()
                    }
                    .padding(bottom = 24.dp),
            )
            Row(
                modifier = Modifier
                    .offset(y = with(localDensity) { restBudgetOffset.toDp() })
                    .alpha(restBudgetAlpha)
                    .onGloballyPositioned {
                        restBudgetHeight = it.size.height.toFloat()
                    },
                verticalAlignment = Alignment.Bottom,
            ) {
                TextWithLabel(
                    modifier = Modifier.padding(bottom = 24.dp),
                    value = restBudgetValue,
                    label = stringResource(id = R.string.rest_budget_for_today),
                    fontSizeValue = restBudgetValueFontSize,
                    fontSizeLabel = restBudgetLabelFontSize,
                )
                AnimatedVisibility(
                    visible = overdaft && currState == AnimState.EDITING,
                    enter = fadeIn() + slideInHorizontally { with(localDensity) { 30.dp.toPx().toInt() } },
                    exit = fadeOut()

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


                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
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
                                            text = stringResource(id = R.string.budget_end),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                } else {
                                    TextWithLabel(
                                        value = budgetPerDaySplit,
                                        label = stringResource(id = R.string.new_daily_budget),
                                        fontSizeValue = MaterialTheme.typography.bodyLarge.fontSize,
                                        fontSizeLabel = MaterialTheme.typography.labelSmall.fontSize,
                                        contentPaddingValues = PaddingValues(0.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun EditorPreview() {
    BuckwheatTheme {
        Editor()
    }
}