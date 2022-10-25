package com.danilkinkin.buckwheat.editor

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
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

enum class AnimState { EDITING, COMMIT, IDLE, RESET }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Editor(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onOpenHistory: (() -> Unit)? = null,
) {
    var currState by remember { mutableStateOf<AnimState?>(null) }

    val textColor = LocalContentColor.current
    val localContext = LocalContext.current
    val localDensity = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val typography = MaterialTheme.typography
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    var stage by remember { mutableStateOf(AnimState.IDLE) }

    var spentValue by remember { mutableStateOf("0") }
    val focusRequester = remember { FocusRequester() }
    var requestFocus by remember { mutableStateOf(false) }

    fun calculateValues() {
        spentValue = spendsViewModel.rawSpentValue.value!!
    }

    observeLiveData(spendsViewModel.dailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.spentFromDailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.stage) {
        when (it) {
            SpendsViewModel.Stage.IDLE, null -> {
                if (currState === AnimState.EDITING) {
                    stage = AnimState.RESET
                }
                focusManager.clearFocus()
            }
            SpendsViewModel.Stage.CREATING_SPENT -> {
                calculateValues()

                stage = AnimState.EDITING
            }
            SpendsViewModel.Stage.EDIT_SPENT -> {
                calculateValues()

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
            EditorToolbar(onOpenHistory = onOpenHistory)
            RestBudget()
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        requestFocus = true
                        calculateValues()

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
                                    fontWeight = FontWeight.W600,
                                    color = textColor.copy(alpha = 0.9f),
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
                                    fontWeight = FontWeight.W600,
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