package com.danilkinkin.buckwheat.editor

import android.animation.ValueAnimator
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min

enum class AnimState { FIRST_IDLE, EDITING, COMMIT, IDLE, RESET }

@OptIn(ExperimentalAnimationApi::class)
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
    var currState by remember { mutableStateOf<AnimState?>(null) }
    var currAnimator by remember { mutableStateOf<ValueAnimator?>(null) }

    val textColor = LocalContentColor.current
    val localContext = LocalContext.current
    val localDensity = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val typography = MaterialTheme.typography
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    var stage by remember { mutableStateOf(AnimState.IDLE) }

    var budgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var restBudgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var spentValue by remember { mutableStateOf("0") }
    var budgetPerDaySplit by remember { mutableStateOf("") }
    var overdaft by remember { mutableStateOf(false) }
    var endBudget by remember { mutableStateOf(false) }

    var warnMessageWidth by remember { mutableStateOf(0F) }

    var editorState by remember { mutableStateOf(EditorState()) }

    val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val focusRequester = remember { FocusRequester() }
    var requestFocus by remember { mutableStateOf(false) }

    fun calculateValues(
        budget: Boolean = true,
        restBudget: Boolean = true,
        spent: Boolean = true
    ) {
        val spentFromDailyBudget = spendsViewModel.spentFromDailyBudget.value!!
        val dailyBudget = spendsViewModel.dailyBudget.value!!

        if (budget) budgetValue = (dailyBudget - spentFromDailyBudget).coerceAtLeast(
            BigDecimal(0)
        )

        if (restBudget) {
            val newBudget = dailyBudget - spentFromDailyBudget - spendsViewModel.currentSpent

            overdaft = newBudget < BigDecimal(0)

            restBudgetValue = newBudget.coerceAtLeast(BigDecimal(0))

            val newPerDayBudget = spendsViewModel.calcBudgetPerDaySplit(
                applyCurrentSpent = true,
                excludeCurrentDay = true,
            )

            endBudget = newPerDayBudget < BigDecimal(0)

            budgetPerDaySplit = prettyCandyCanes(
                newPerDayBudget.coerceAtLeast(BigDecimal(0)),
                currency = currency,
            )
        }

        if (spent) {
            spentValue = spendsViewModel.rawSpentValue.value!!
        }
    }

    fun animTo(state: AnimState) {
        if (currState == state) return

        currState = state

        if (currAnimator !== null) {
            currAnimator!!.pause()
        }

        currAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            duration = 220
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { valueAnimator ->
                editorState = animFrame(
                    editorState,
                    state,
                    valueAnimator.animatedValue as Float,
                )
            }

            doOnEnd {
                if (state === AnimState.COMMIT) {
                    editorState = animFrame(editorState, AnimState.IDLE)
                    calculateValues(restBudget = false)
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
                if (currState === AnimState.EDITING) {
                    stage = AnimState.RESET
                }
                focusManager.clearFocus()
                calculateValues(budget = false)
            }
            SpendsViewModel.Stage.CREATING_SPENT -> {
                calculateValues(budget = false)

                stage = AnimState.EDITING
            }
            SpendsViewModel.Stage.EDIT_SPENT -> {
                calculateValues(budget = false)

                stage = AnimState.EDITING
            }
            SpendsViewModel.Stage.COMMITTING_SPENT -> {
                stage = AnimState.COMMIT

                spendsViewModel.resetSpent()
                focusManager.clearFocus()
            }
        }

        currState = stage
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxHeight()
        ) {
            EditorToolbar(
                onOpenWallet = onOpenWallet,
                onOpenSettings = onOpenSettings,
                onDebugMenu = onDebugMenu,
                onOpenHistory = onOpenHistory,
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 0.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BudgetEndWarn(
                    overdaft = overdaft,
                    forceShow = currState == AnimState.EDITING,
                    endBudget = endBudget,
                    budgetPerDaySplit = budgetPerDaySplit,
                    modifier = Modifier
                        .onGloballyPositioned {
                            warnMessageWidth = it.size.width.toFloat()
                        }
                )
                Spacer(Modifier.width(16.dp))
                if (stage != AnimState.IDLE) {
                    TextWithLabel(
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        value = prettyCandyCanes(
                            restBudgetValue,
                            currency = currency,
                        ),
                        label = stringResource(id = R.string.rest_budget_for_today),
                        fontSizeValue = MaterialTheme.typography.headlineSmall.fontSize,
                        fontSizeLabel = MaterialTheme.typography.labelMedium .fontSize,
                        horizontalAlignment = Alignment.End,
                    )
                } else {
                    TextWithLabel(
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        value = prettyCandyCanes(
                            budgetValue,
                            currency = currency,
                        ),
                        label = stringResource(id = R.string.budget_for_today),
                        fontSizeValue = MaterialTheme.typography.headlineSmall.fontSize,
                        fontSizeLabel = MaterialTheme.typography.labelMedium.fontSize,
                        horizontalAlignment = Alignment.End,
                    )
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        requestFocus = true
                        stage = AnimState.EDITING
                    },
            ) {
                val maxHeight = with(localDensity) {
                    constraints.maxHeight.toDp()
                }

                val maxFontSize = min(
                    calcMaxFont(with(localDensity) { maxHeight.toPx() }),
                    100.sp,
                )

                Log.d("stage", stage.toString())

                val smallShift = with(localDensity) { 30.dp.toPx().toInt() }
                val bigShift = with(localDensity) { 40.dp.toPx().toInt() }

                val currencyShiftWidth by remember(currency) {
                    val output = prettyCandyCanes(
                        0.toBigDecimal(),
                        currency,
                        maximumFractionDigits = 0,
                        minimumFractionDigits = 0,
                    )

                    val currSymbol = output.filter { it !='0' }

                    val startWithCurr = output.startsWith(currSymbol)

                    if (!startWithCurr) {
                        return@remember mutableStateOf(0)
                    }

                    val intrinsics = ParagraphIntrinsics(
                        text = "$currSymbol  ",
                        style = typography.labelMedium.copy(
                            fontSize = maxFontSize,
                        ),
                        spanStyles = listOf(AnnotatedString.Range(
                            SpanStyle(fontSize = typography.displaySmall.fontSize),
                            currSymbol.length,
                            currSymbol.length + 2,
                        )),
                        density = localDensity,
                        fontFamilyResolver = createFontFamilyResolver(localContext)
                    )

                    mutableStateOf(intrinsics.maxIntrinsicWidth.toInt())
                }

                AnimatedContent(
                    targetState = stage,
                    transitionSpec = {
                        when(targetState) {
                            AnimState.EDITING -> slideInHorizontally(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInOutQuad,
                                )
                            ) {
                                -currencyShiftWidth
                            } + fadeIn(
                                tween(durationMillis = 150)
                            ) with slideOutHorizontally(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInOutQuad,
                                )
                            ) {
                                currencyShiftWidth
                            } + fadeOut(
                                tween(durationMillis = 150)
                            )
                            AnimState.COMMIT -> slideInVertically(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInQuad,
                                )
                            ) { smallShift } + fadeIn(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInQuad,
                                )
                            ) with slideOutVertically(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInQuad,
                                )
                            ) { -bigShift } + fadeOut(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInQuad,
                                )
                            )
                            AnimState.RESET -> fadeIn(
                                tween(
                                    durationMillis = 50,
                                    easing = EaseInQuad,
                                )
                            ) with fadeOut(
                                tween(
                                    durationMillis = 50,
                                    easing = EaseInQuad,
                                )
                            )
                            else -> fadeIn(
                                tween(durationMillis = 50)
                            ) with fadeOut(
                                tween(durationMillis = 50)
                            )
                        }.using(
                            SizeTransform(clip = false)
                        )
                    }
                ) { targetStage ->
                    if (targetStage !== AnimState.EDITING) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                    text = "Enter spent",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                                    color = textColor.copy(alpha = 0.6f),
                                    overflow = TextOverflow.Visible,
                                    softWrap = false,
                                    modifier = Modifier
                                        .padding(start = 36.dp, end = 36.dp)
                                        .offset(x = (-2).dp),
                                )
                                Text(
                                    text = stringResource(id = R.string.spent),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                    color = textColor.copy(alpha = 0f),
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                    modifier = Modifier.padding(start = 36.dp, end = 36.dp),
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            EditableTextWithLabel(
                                value = spentValue,
                                label = stringResource(id = R.string.spent),
                                placeholder = "  Enter spent",
                                onChangeValue = {
                                    val converted = tryConvertStringToNumber(it)

                                    spendsViewModel.rawSpentValue.value = it

                                    spendsViewModel.editSpent(converted.join().toBigDecimal())

                                    if (it === "") {
                                        runBlocking {
                                            spendsViewModel.resetSpent()
                                        }
                                    }
                                },
                                currency = currency,
                                fontSizeValue = maxFontSize,
                                fontSizeLabel = MaterialTheme.typography.labelLarge.fontSize,
                                focusRequester = focusRequester,
                                placeholderStyle = SpanStyle(
                                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                                    baselineShift = BaselineShift(0.23f)
                                ),
                            )

                            LaunchedEffect(Unit) {
                                if (requestFocus) focusRequester.requestFocus()
                                requestFocus = false
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RowState(
    val valueFontSize: Float,
    val labelFontSize: Float,
    val offset: Float,
    var height: Float,
    val alpha: Float,
)

data class EditorState(
    val budget: RowState = RowState(
        valueFontSize = 0.375f,
        labelFontSize = 0.2f,
        offset = 0f,
        height = 1000f,
        alpha = 0f
    ),
    val spent: RowState = RowState(
        valueFontSize = 0.375f,
        labelFontSize = 0.2f,
        offset = 0f,
        height = 1000f,
        alpha = 0f
    ),
    val restBudget: RowState = RowState(
        valueFontSize = 0.375f,
        labelFontSize = 0.2f,
        offset = 0f,
        height = 1000f,
        alpha = 0f
    ),
)

fun animFrame(rowState: EditorState, state: AnimState, progress: Float = 1F): EditorState {
    return when (state) {
        AnimState.FIRST_IDLE -> {
            return rowState.copy(
                budget = rowState.budget.copy(
                    valueFontSize = 1f,
                    labelFontSize = 0.25f,
                    offset = 60.dp.value * (1F - progress),
                    alpha = progress
                )
            )
        }
        AnimState.EDITING -> {
            var offset = rowState.restBudget.height

            val restBudget = rowState.restBudget.copy(
                valueFontSize = 0.375f,
                labelFontSize = 0.2f,
                offset = (offset + rowState.spent.height) * (1f - progress),
                alpha = 1f
            )

            val spent = rowState.spent.copy(
                valueFontSize = 1f,
                labelFontSize = 0.25f,
                offset = (offset + rowState.spent.height) * (1f - progress) - offset,
                alpha = 1f
            )

            offset += rowState.spent.height

            val budget = rowState.budget.copy(
                valueFontSize = 1f - 0.75f * progress,
                labelFontSize = 0.25f -0.125f * progress,
                offset = -offset * progress,
                alpha = 1f
            )

            rowState.copy(
                budget = budget,
                spent = spent,
                restBudget = restBudget,
            )
        }
        AnimState.COMMIT -> {
            val progressA = min(progress * 2F, 1F)
            val progressB = max((progress - 0.5F) * 2F, 0F)
            var offset = rowState.restBudget.height

            val restBudget = rowState.restBudget.copy(
                valueFontSize = 0.375f + 0.625f * progress,
                labelFontSize = 0.2f + 0.05f * progress,
                alpha = 1f
            )

            val spent = rowState.spent.copy(
                valueFontSize = 1f,
                labelFontSize = 0.25f,
                offset = -offset - 50 * progressB,
                alpha = 1f - progressB
            )

            offset += rowState.spent.height

            val budget = rowState.budget.copy(
                valueFontSize = 0.25f,
                labelFontSize = 0.125f,
                offset = -offset - 50 * progressA,
                alpha = 1f - progressA
            )


            rowState.copy(
                budget = budget,
                spent = spent,
                restBudget = restBudget,
            )
        }
        AnimState.RESET -> {
            var offset = rowState.restBudget.height

            val restBudget = rowState.restBudget.copy(
                valueFontSize = 0.375f,
                labelFontSize = 0.2f,
                offset = (offset + rowState.spent.height) * progress,
            )

            val spent = rowState.spent.copy(
                valueFontSize = 1f,
                labelFontSize = 0.25f,
                offset = (rowState.spent.height + offset) * progress - offset,
            )

            offset += rowState.spent.height

            val budget = rowState.budget.copy(
                valueFontSize = 0.25f + 0.75f * progress,
                labelFontSize = 0.125f + 0.125f * progress,
                offset = -offset * (1F - progress),
            )


            rowState.copy(
                budget = budget,
                spent = spent,
                restBudget = restBudget,
            )
        }
        AnimState.IDLE -> {
            rowState.copy(
                budget = rowState.budget.copy(
                    valueFontSize = 1f,
                    labelFontSize = 0.25f,
                    offset = 0f,
                    alpha = 1f,
                ),
                restBudget = rowState.restBudget.copy(
                    alpha = 0f,
                ),
            )
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