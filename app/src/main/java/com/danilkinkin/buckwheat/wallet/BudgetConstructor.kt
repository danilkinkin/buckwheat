package com.danilkinkin.buckwheat.wallet

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BudgetConstructor(
    requestFinishDate: ((presetDate: Date, callback: (finishDate: Date) -> Unit) -> Unit) = { _: Date, _: (finishDate: Date) -> Unit -> },
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onChange: (budget: BigDecimal, finishDate: Date) -> Unit = { _, _ -> },
) {
    var rawBudget by remember {
        val restBudget =
            (spendsViewModel.budget.value!! - spendsViewModel.spent.value!! - spendsViewModel.spentFromDailyBudget.value!!)

        val converted = if (restBudget !== BigDecimal(0)) {
            tryConvertStringToNumber(restBudget.toString())
        } else {
            Triple("", "0", "")
        }

        mutableStateOf(converted.first + converted.second)
    }
    var budget by remember {
        val restBudget =
            (spendsViewModel.budget.value!! - spendsViewModel.spent.value!! - spendsViewModel.spentFromDailyBudget.value!!)

        mutableStateOf(restBudget)
    }
    val dateToValue = remember { mutableStateOf(spendsViewModel.finishDate) }
    var showUseBudgetSuggestion by remember { mutableStateOf(true) }
    var showUseLifetimeSuggestion by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        val days = countDays(dateToValue.value)
        BasicTextField(
            value = rawBudget,
            onValueChange = {
                val converted = tryConvertStringToNumber(it)

                rawBudget = converted.join(third = false)
                budget = converted.join().toBigDecimal()

                onChange(budget, dateToValue.value)
            },
            textStyle = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            visualTransformation = visualTransformationAsCurrency(
                currency = ExtendCurrency(type = CurrencyType.NONE),
                hintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { input ->
                Column {
                    TextRow(
                        icon = painterResource(R.drawable.ic_money),
                        text = stringResource(R.string.label_budget),
                        endContent = {
                            Log.d("budget", "budget = ${budget} mbudget = ${spendsViewModel.budget.value!!}")

                            if (
                                budget == spendsViewModel.budget.value!!
                                || spendsViewModel.budget.value!! == BigDecimal(0)
                            ) return@TextRow

                            UseLastSuggestionChip(
                                visible = showUseBudgetSuggestion,
                                icon = painterResource(R.drawable.ic_money),
                                onClick = {
                                    rawBudget =
                                        if (spendsViewModel.budget.value!! !== BigDecimal(0)) {
                                            tryConvertStringToNumber(spendsViewModel.budget.value!!.toString()).join(
                                                third = false
                                            )
                                        } else {
                                            Triple("", "0", "").join(third = false)
                                        }

                                    budget = spendsViewModel.budget.value!!

                                    onChange(
                                        budget,
                                        dateToValue.value,
                                    )

                                    showUseBudgetSuggestion = false
                                }
                            )
                        },
                    )
                    Box(Modifier.padding(start = 56.dp, bottom = 12.dp)) {
                        input()
                    }
                }
            },
        )
        Divider()
        ButtonRow(
            icon = painterResource(R.drawable.ic_calendar),
            text = if (days > 0) {
                String.format(
                    pluralStringResource(R.plurals.finish_date_label, days),
                    prettyDate(dateToValue.value, showTime = false, forceShowDate = true),
                    days,
                )
            } else {
                stringResource(R.string.finish_date_not_select)
            },
            onClick = {
                coroutineScope.launch {
                    requestFinishDate(dateToValue.value) {
                        dateToValue.value = it

                        onChange(budget, dateToValue.value)
                    }
                }
            },
            endContent = {
                Log.d("times", "startDate = ${spendsViewModel.startDate} finishDate = ${spendsViewModel.finishDate}")

                val length = countDays(
                    spendsViewModel.finishDate,
                    spendsViewModel.startDate,
                )
                val finishDate = LocalDate.now().plusDays(length.toLong()).toDate()

                if (
                    isSameDay(spendsViewModel.startDate.time, spendsViewModel.finishDate.time)
                    || isSameDay(finishDate.time, spendsViewModel.finishDate.time)
                ) return@ButtonRow

                UseLastSuggestionChip(
                    visible = showUseLifetimeSuggestion,
                    icon = painterResource(R.drawable.ic_calendar),
                    onClick = {

                        dateToValue.value = finishDate

                        onChange(
                            budget,
                            finishDate,
                        )

                        showUseLifetimeSuggestion = false
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UseLastSuggestionChip(
    visible: Boolean,
    icon: Painter,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            tween(durationMillis = 350)
        ) + scaleIn(
            animationSpec = tween(durationMillis = 350)
        ),
        exit = fadeOut(
            tween(durationMillis = 350)
        ) + scaleOut(
            animationSpec = tween(durationMillis = 350)
        ),
    ) {
        SuggestionChip(
            icon = {
                Icon(
                    painter = icon,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null,
                )
            },
            label = {
                Text(text = stringResource(R.string.use_last))
            },
            onClick = {
                onClick()
            }
        )
    }
}

