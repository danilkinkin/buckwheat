package com.danilkinkin.buckwheat.patterns

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.ai.AiInsightResult
import com.danilkinkin.buckwheat.data.categories.offlineCategoryOrNull
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.data.entities.toTransaction
import com.danilkinkin.buckwheat.di.GetCurrentDateUseCase
import com.danilkinkin.buckwheat.di.SpendsRepository
import com.danilkinkin.buckwheat.util.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface PatternsUiState {
    data object Idle : PatternsUiState
    data object Loading : PatternsUiState

    // The offline engine always produces a report first; when AI is configured it upgrades the
    // narrative in the background (aiLoading) and swaps in the AI text on success — the same
    // swap-in as AiInsightViewModel. `availableMonths` bounds the window stepper.
    data class Report(
        val dataset: PatternDataset,
        val window: PatternWindow,
        val availableMonths: Int,
        val metrics: PatternMetrics,
        val narrative: String,
        val isAi: Boolean,
        val aiFailure: String?,
        val aiLoading: Boolean,
    ) : PatternsUiState

    data class Error(val message: String) : PatternsUiState
}

// Everything the ViewModel reads in one pass, so the engine runs off a single snapshot of the
// repository. Charges carry comments (on-device only) and are kept out of the PatternDataset,
// matching the Phase 2 privacy posture.
internal data class PatternDataLoad(
    val dataset: PatternDataset,
    val charges: List<PatternCharge>,
    val dailyBudget: BigDecimal,
)

@HiltViewModel
class PatternsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spendsRepository: SpendsRepository,
    private val getCurrentDateUseCase: GetCurrentDateUseCase,
) : ViewModel() {
    private val _state = MutableLiveData<PatternsUiState>(PatternsUiState.Idle)
    val state: LiveData<PatternsUiState> = _state

    private var lastAiWindow: PatternWindow? = null

    // Analyzes a fresh snapshot for the given window: publishes the offline report instantly,
    // then upgrades the narrative with AI in the background. Guards against concurrent runs.
    fun generate(window: PatternWindow = PatternWindow(months = 6, allData = false)) {
        val current = _state.value
        if (current is PatternsUiState.Loading) return
        if (current is PatternsUiState.Report && current.aiLoading) return
        viewModelScope.launch {
            _state.value = PatternsUiState.Loading
            val loaded = runCatching { withContext(Dispatchers.Default) { loadInputs() } }.getOrNull()
            if (loaded == null) {
                _state.value = PatternsUiState.Error("Could not load spend data")
                return@launch
            }
            val available = availableMonths(loaded.dataset)
            val effectiveWindow = clampWindow(window, available)
            val windowed = applyWindow(loaded.dataset, effectiveWindow)
            val charges = windowedCharges(loaded.charges, loaded.dataset, effectiveWindow)
            val metrics = withContext(Dispatchers.Default) {
                analyzePatterns(windowed, loaded.dailyBudget, charges)
            }
            val offlineReport = PatternsUiState.Report(
                dataset = windowed,
                window = effectiveWindow,
                availableMonths = available,
                metrics = metrics,
                narrative = metrics.report,
                isAi = false,
                aiFailure = null,
                aiLoading = true,
            )
            _state.value = offlineReport
            lastAiWindow = effectiveWindow
            val ai = runCatching {
                generatePatternAiInsight(context, buildPatternAiSummary(windowed, metrics))
            }.getOrNull()
            // Only apply when the page still shows this window's report and is still waiting for
            // AI — a setWindow() in the meantime has already produced a fresh offline report.
            val latest = _state.value
            if (latest is PatternsUiState.Report &&
                latest.window == effectiveWindow && latest.aiLoading
            ) {
                _state.value = when (ai) {
                    is AiInsightResult.Success -> latest.copy(
                        narrative = ai.report,
                        isAi = true,
                        aiFailure = null,
                        aiLoading = false,
                    )
                    is AiInsightResult.Failure -> latest.copy(
                        aiFailure = ai.message,
                        aiLoading = false,
                    )
                    AiInsightResult.NotConfigured -> latest.copy(aiLoading = false)
                    null -> latest.copy(
                        aiFailure = "unknown error",
                        aiLoading = false,
                    )
                }
            }
        }
    }

    // Re-runs the pure engine only. Keeps the last AI narrative across a window switch so a
    // browsing the page never silently fires the model again (no surprise spend) — the user can
    // regenerate explicitly.
    fun setWindow(window: PatternWindow) {
        val current = _state.value
        if (current is PatternsUiState.Loading) return
        val previous = current as? PatternsUiState.Report
        if (previous == null) {
            generate(window)
            return
        }
        if (previous.window == window) return
        val keepAi = previous.isAi && previous.narrative.isNotBlank()
        viewModelScope.launch {
            _state.value = PatternsUiState.Loading
            val loaded = runCatching { withContext(Dispatchers.Default) { loadInputs() } }.getOrNull()
            if (loaded == null) {
                _state.value = PatternsUiState.Error("Could not load spend data")
                return@launch
            }
            val available = availableMonths(loaded.dataset)
            val effectiveWindow = clampWindow(window, available)
            val windowed = applyWindow(loaded.dataset, effectiveWindow)
            val charges = windowedCharges(loaded.charges, loaded.dataset, effectiveWindow)
            val metrics = withContext(Dispatchers.Default) {
                analyzePatterns(windowed, loaded.dailyBudget, charges)
            }
            _state.value = PatternsUiState.Report(
                dataset = windowed,
                window = effectiveWindow,
                availableMonths = available,
                metrics = metrics,
                narrative = if (keepAi) previous.narrative else metrics.report,
                isAi = keepAi,
                aiFailure = null,
                aiLoading = false,
            )
            lastAiWindow = null
        }
    }

    private fun clampWindow(window: PatternWindow, availableMonths: Int): PatternWindow =
        if (window.allData) window else window.copy(months = window.months.coerceIn(1, availableMonths))

    // Charges must follow the same window as the spends, otherwise a subscription last seen
    // months outside the chosen range would still show up as a recurring charge.
    private fun windowedCharges(
        charges: List<PatternCharge>,
        dataset: PatternDataset,
        window: PatternWindow,
    ): List<PatternCharge> {
        val start = windowStartDate(dataset, window) ?: return charges
        return charges.filter { !it.date.before(start) }
    }

    private suspend fun loadInputs(): PatternDataLoad {
        val today = getCurrentDateUseCase().toLocalDate()
        val spends = spendsRepository.getAllSpends().asFlow().first()
        val archived = spendsRepository.getAllArchivedTransactions().asFlow().first()
        val periods = spendsRepository.getAllBudgetPeriods().asFlow().first()
        val currency = spendsRepository.getCurrency().first().value ?: ""

        val currentSpends = spends.map { toPatternSpend(it) }
        val archivedSpends = archived
            .filter { it.type == TransactionType.SPENT }
            .map { toPatternSpend(it.toTransaction()) }

        // The recurring-charge detector is the ONLY consumer of comments: pure on-device, never
        // part of a PatternDataset, so the AI prompt builder structurally cannot reach them.
        val charges = (spends + archived.map { it.toTransaction() })
            .filter { it.type == TransactionType.SPENT && it.comment.isNotBlank() }
            .map { PatternCharge(it.date, it.value, it.comment) }

        val periodList = periods.map {
            PatternPeriod(
                start = it.startDate,
                finish = it.finishDate,
                budget = it.budget,
                totalSpent = it.totalSpent,
                isImported = it.isImported,
            )
        } + currentPeriod()

        val dataset = PatternDataset(
            spends = (currentSpends + archivedSpends).sortedBy { it.date },
            periods = periodList,
            currencyCode = currency,
            today = today,
        )
        return PatternDataLoad(dataset, charges, spendsRepository.getDailyBudget().first())
    }

    private suspend fun currentPeriod(): PatternPeriod {
        val start = spendsRepository.getStartPeriodDate().first()
        val finish = spendsRepository.getFinishPeriodActualDate().first()
            ?: spendsRepository.getFinishPeriodDate().first()
            ?: getCurrentDateUseCase()
        return PatternPeriod(
            start = start,
            finish = finish,
            budget = spendsRepository.getBudget().first(),
            totalSpent = spendsRepository.getSpent().first(),
            isImported = false,
        )
    }

    // The stored category string when present; otherwise the offline keyword guess. Unresolved
    // (blank comment, no keyword) stays null so the engine buckets it under "Other".
    private fun toPatternSpend(tx: Transaction): PatternSpend = PatternSpend(
        date = tx.date,
        value = tx.value,
        category = tx.category?.takeIf { it.isNotBlank() } ?: offlineCategoryOrNull(tx.comment)?.name,
    )
}
