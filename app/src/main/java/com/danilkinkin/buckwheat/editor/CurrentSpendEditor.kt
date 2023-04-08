package com.danilkinkin.buckwheat.editor

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.runBlocking

class FocusController {
    var onFocus: MutableState<(() -> Unit)?> = mutableStateOf(null)
    var onBlur: MutableState<(() -> Unit)?> = mutableStateOf(null)

    fun focus() {
        onFocus.value?.let { it() }
    }

    fun blur() {
        onBlur.value?.let { it() }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CurrentSpendEditor(
    modifier: Modifier = Modifier,
    focusController: FocusController = remember { FocusController() },
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val textColor = LocalContentColor.current
    val localContext = LocalContext.current
    val localDensity = LocalDensity.current
    val focusManager = LocalFocusManager.current

    val typography = MaterialTheme.typography
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val mode by spendsViewModel.mode.observeAsState(SpendsViewModel.Mode.ADD)

    var spentValue by remember { mutableStateOf("0") }
    var stage by remember { mutableStateOf(AnimState.IDLE) }
    var currState by remember { mutableStateOf<AnimState?>(null) }
    var requestFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

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

                focusManager.clearFocus()
            }
        }

        currState = stage
    }

    LaunchedEffect(focusController) {
        focusController.onFocus.value = {
            if (currState != AnimState.EDITING) {
                requestFocus = true
                calculateValues()

                spendsViewModel.createSpent()
                spendsViewModel.editSpent(0.toBigDecimal())
            }
        }
        focusController.onBlur.value = {
            focusManager.clearFocus()
        }
    }

    BoxWithConstraints(modifier) {
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

            if (!startWithCurr || currSymbol.isEmpty()) {
                return@remember mutableStateOf(0)
            }

            val intrinsics = ParagraphIntrinsics(
                text = "$currSymbol  ",
                style = typography.labelMedium.copy(
                    fontSize = maxFontSize,
                ),
                spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(fontSize = typography.displaySmall.fontSize),
                        currSymbol.length,
                        currSymbol.length + 2,
                    )),
                density = localDensity,
                fontFamilyResolver = createFontFamilyResolver(localContext)
            )

            mutableStateOf(intrinsics.maxIntrinsicWidth.toInt())
        }

        Box(
            modifier = Modifier.fillMaxSize().background(Color.Green.copy(alpha = 0.3f)),
            contentAlignment = Alignment.CenterEnd,
        ) {
            EditableTextWithLabel(
                value = spentValue,
                label = if (mode === SpendsViewModel.Mode.ADD) {
                    stringResource(R.string.new_spent)
                } else {
                    stringResource(R.string.edit_spent)
                },
                placeholder = "",
                onChangeValue = {
                    val fixed = fixedNumberString(it)
                    val converted = tryConvertStringToNumber(fixed)

                    Log.d("onChangeValue", "it = $it")

                    spendsViewModel.rawSpentValue.value = fixed
                    spendsViewModel.editSpent(converted.join().toBigDecimal())

                    if (fixed === "") {
                        if (mode === SpendsViewModel.Mode.ADD) runBlocking {
                            spendsViewModel.resetSpent()
                        }
                    }
                },
                currency = currency,
                fontSizeValue = maxFontSize,
                fontSizeLabel = MaterialTheme.typography.labelLarge.fontSize,
                focusRequester = focusRequester,
                placeholderStyle = SpanStyle(
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    fontWeight = FontWeight.W700,
                    baselineShift = BaselineShift(0.26f)
                ),
                currencyStyle = SpanStyle(
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    fontWeight = FontWeight.W700,
                    baselineShift = BaselineShift(0f)
                ),
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                requestFocus = false
            }
        }
    }
}