package com.danilkinkin.buckwheat.editor

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

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
                //calculateValues(budget = false)
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
        Column(Modifier.fillMaxHeight()) {
            EditorToolbar(
                onOpenWallet = onOpenWallet,
                onOpenSettings = onOpenSettings,
                onDebugMenu = onDebugMenu,
                onOpenHistory = onOpenHistory,
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                val alpha: Float by animateFloatAsState(
                    if (restBudgetValue > 0.toBigDecimal()) 1f else 0f,
                    tween(
                        durationMillis = 150,
                        easing = EaseInOutQuad,
                    ),
                )

                if (stage != AnimState.IDLE) {
                    TextWithLabel(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 16.dp)
                            .alpha(alpha)
                            .offset(x = (-30).dp * (1f - alpha)),
                        value = prettyCandyCanes(
                            restBudgetValue,
                            currency = currency,
                        ),
                        label = stringResource(id = R.string.rest_budget_for_today),
                        fontSizeValue = MaterialTheme.typography.headlineSmall.fontSize,
                        fontSizeLabel = MaterialTheme.typography.labelMedium.fontSize,
                        horizontalAlignment = Alignment.End,
                    )
                } else {
                    TextWithLabel(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 16.dp)
                            .alpha(alpha)
                            .offset(x = (-30).dp * (1f - alpha)),
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
                BudgetEndWarn(
                    overdaft = overdaft,
                    forceShow = restBudgetValue <= 0.toBigDecimal() || currState == AnimState.EDITING,
                    endBudget = endBudget,
                    budgetPerDaySplit = budgetPerDaySplit,
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp)
                )
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
                        calculateValues(budget = false)

                        spendsViewModel.createSpent()
                        spendsViewModel.editSpent(0.toBigDecimal())
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
                            AnimState.RESET -> slideInHorizontally(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInOutQuad,
                                )
                            ) {
                                currencyShiftWidth
                            } + fadeIn(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInQuad,
                                )
                            ) with slideOutHorizontally(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInOutQuad,
                                )
                            ) {
                                -currencyShiftWidth
                            } + fadeOut(
                                tween(
                                    durationMillis = 150,
                                    easing = EaseInQuad,
                                )
                            )
                            else -> fadeIn(
                                tween(durationMillis = 150)
                            ) with fadeOut(
                                tween(durationMillis = 150)
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
                                    text = stringResource(R.string.enter_spent_placeholder),
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
                                placeholder = "  ${stringResource(R.string.enter_spent_placeholder)}",
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

@Preview
@Composable
fun EditorPreview() {
    BuckwheatTheme {
        Editor()
    }
}