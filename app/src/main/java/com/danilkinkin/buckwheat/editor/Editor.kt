package com.danilkinkin.buckwheat.editor

import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.hilt.navigation.compose.hiltViewModel
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

    val localDensity = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())

    var budgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var restBudgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var spentValue by remember { mutableStateOf("0") }
    var budgetPerDaySplit by remember { mutableStateOf("") }
    var overdaft by remember { mutableStateOf(false) }
    var endBudget by remember { mutableStateOf(false) }

    var warnMessageWidth by remember { mutableStateOf(0F) }

    var editorState by remember { mutableStateOf(EditorState()) }


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
            duration = 2200
            interpolator = AccelerateDecelerateInterpolator()

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
                if (currState === AnimState.EDITING) animTo(AnimState.RESET)
                focusManager.clearFocus()
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

                spendsViewModel.resetSpent()
                focusManager.clearFocus()
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    if (currState === null) animTo(AnimState.FIRST_IDLE)
                },
        ) {
            TextWithLabel(
                value = prettyCandyCanes(
                    budgetValue,
                    currency = currency,
                ),
                label = stringResource(id = R.string.budget_for_today),
                fontSizeValue = editorState.budget.valueFontSize,
                fontSizeLabel = editorState.budget.labelFontSize,
                modifier = Modifier
                    .offset(y = with(localDensity) { editorState.budget.offset.toDp() })
                    .alpha(editorState.budget.alpha)
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
                currency = currency,
                fontSizeValue = editorState.spent.valueFontSize,
                fontSizeLabel = editorState.spent.labelFontSize,
                modifier = Modifier
                    .offset(y = with(localDensity) { editorState.spent.offset.toDp() })
                    .alpha(editorState.spent.alpha)
                    .onGloballyPositioned {
                        if (editorState.spent.alpha == 0f) return@onGloballyPositioned

                        editorState = editorState.copy(
                            spent = editorState.spent.copy(
                                height = it.size.height.toFloat()
                            )
                        )
                    }
                    .padding(bottom = 24.dp),
            )
            Row(
                modifier = Modifier
                    .offset(y = with(localDensity) { editorState.restBudget.offset.toDp() })
                    .alpha(editorState.restBudget.alpha)
                    .onGloballyPositioned {
                        if (editorState.restBudget.alpha == 0f) return@onGloballyPositioned

                        editorState = editorState.copy(
                            restBudget = editorState.restBudget.copy(
                                height = it.size.height.toFloat()
                            )
                        )
                    },
                verticalAlignment = Alignment.Bottom,
            ) {
                TextWithLabel(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .weight(1f),
                    value = prettyCandyCanes(
                        restBudgetValue,
                        currency = currency,
                    ),
                    label = stringResource(id = R.string.rest_budget_for_today),
                    fontSizeValue = editorState.restBudget.valueFontSize,
                    fontSizeLabel = editorState.restBudget.labelFontSize,
                )

                Spacer(Modifier.requiredWidth(with(localDensity) { warnMessageWidth.toDp() }))
            }

            BoxWithConstraints(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd,
            ) {
                val maxWidth = with(localDensity) { (constraints.maxWidth / 2f).toDp() }

                Row(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    BudgetEndWarn(
                        overdaft = overdaft,
                        forceShow = currState == AnimState.EDITING,
                        endBudget = endBudget,
                        budgetPerDaySplit = budgetPerDaySplit,
                        modifier = Modifier
                            .onGloballyPositioned {
                                warnMessageWidth = it.size.width.toFloat()
                            }
                            .widthIn(max = maxWidth)
                            .padding(
                                top = 24.dp,
                                end = 24.dp,
                                bottom = 24.dp,
                            )
                    )
                }
            }
        }
        EditorToolbar(
            onOpenWallet = onOpenWallet,
            onOpenSettings = onOpenSettings,
            onDebugMenu = onDebugMenu,
            onOpenHistory = onOpenHistory,
        )
    }
}

data class RowState(
    val valueFontSize: TextUnit,
    val labelFontSize: TextUnit,
    val offset: Float,
    var height: Float,
    val alpha: Float,
)

data class EditorState(
    val budget: RowState = RowState(
        valueFontSize = 30.sp,
        labelFontSize = 16.sp,
        offset = 0f,
        height = 1000f,
        alpha = 0f
    ),
    val spent: RowState = RowState(
        valueFontSize = 30.sp,
        labelFontSize = 16.sp,
        offset = 0f,
        height = 1000f,
        alpha = 0f
    ),
    val restBudget: RowState = RowState(
        valueFontSize = 30.sp,
        labelFontSize = 16.sp,
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
                    valueFontSize = 80.sp,
                    labelFontSize = 20.sp,
                    offset = 60.dp.value * (1F - progress),
                    alpha = progress
                )
            )
        }
        AnimState.EDITING -> {
            var offset = rowState.restBudget.height

            val restBudget = rowState.restBudget.copy(
                valueFontSize = 30.sp,
                labelFontSize = 16.sp,
                offset = (offset + rowState.spent.height) * (1f - progress),
                alpha = 1f
            )

            Log.d("spend calc", "progress=$progress altProgress=${1f - progress} offset=$offset h=${rowState.spent.height} res=${(offset + rowState.spent.height) * (1f - progress) - offset}")

            val spent = rowState.spent.copy(
                valueFontSize = 80.sp,
                labelFontSize = 20.sp,
                offset = (offset + rowState.spent.height) * (1f - progress) - offset,
                alpha = 1f
            )

            offset += rowState.spent.height

            val budget = rowState.budget.copy(
                valueFontSize = (80 - 60 * progress).sp,
                labelFontSize = (20 - 10 * progress).sp,
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
                valueFontSize = (30 + 50 * progress).sp,
                labelFontSize = (16 + 4 * progress).sp,
                alpha = 1f
            )

            val spent = rowState.spent.copy(
                valueFontSize = 80.sp,
                labelFontSize = 20.sp,
                offset = -offset - 50 * progressB,
                alpha = 1f - progressB
            )

            offset += rowState.spent.height

            val budget = rowState.budget.copy(
                valueFontSize = 20.sp,
                labelFontSize = 10.sp,
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
                valueFontSize = 30.sp,
                labelFontSize = 16.sp,
                offset = (offset + rowState.spent.height) * progress,
            )

            val spent = rowState.spent.copy(
                valueFontSize = 80.sp,
                labelFontSize = 20.sp,
                offset = (rowState.spent.height + offset) * progress - offset,
            )

            offset += rowState.spent.height

            val budget = rowState.budget.copy(
                valueFontSize = (20 + 60 * progress).sp,
                labelFontSize = (10 + 10 * progress).sp,
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
                    valueFontSize = 80.sp,
                    labelFontSize = 20.sp,
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