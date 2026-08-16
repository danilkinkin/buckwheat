# Spending Pattern Engine + "Spending Patterns" page (plan)

## Goal

Turn the user's 6-8 months of history (active `transactions` + archived `budget_periods` /
`archived_transactions`) into an **analytical pattern engine** and a brand-new, richly designed
**"Spending Patterns"** page that:

- Reviews the full history and produces real, analytical insights (trends, category behavior,
  weekday/day-of-month rhythms, budget compliance, anomalies, forecasts).
- Tells the user **what to optimize** and **where/how money goes** with actionable, ranked
  suggestions.
- Uses the existing AI infrastructure (OpenAI-compatible `callAi` router) to upgrade the narrative
  to plain-language analysis, **always with an offline fallback** so the page never breaks.
- Is a **totally new page** (new sheet screen), visually distinct: gradient area line charts,
  tap-to-inspect graphs, a topographic contour background, variable-font typography, and subtle
  entrance/scroll animations.

The engine is **pure and unit-testable** (repo convention), the UI reuses the existing analytics
chart composables as "graph images", and the AI layer follows the proven offline-first pattern from
`AiInsightViewModel`.

---

## Data available today (no schema change needed)

| Source | Table / key | Used for |
|--------|-------------|----------|
| `Transaction` (Room) | `transactions` — active period | Current + recent spends (`type=SPENT`), date, value, comment, category |
| `ArchivedTransaction` (Room) | `archived_transactions` — past periods | Historical spends, category persisted since DB v14 |
| `BudgetPeriod` (Room) | `budget_periods` | Per-period budget, dates, `totalSpent`, `isImported`, currency |
| `SavedCategory` (Room) | `saved_categories` | Custom category names + emoji (chart labels) |
| DataStore | `budget` / `dailyBudget` / `spent` / period dates / `currency` | Current-period context for "projection vs budget" |
| `SpendsRepository` flows | read-only API | Single entry point for all of the above |

Existing DAO suspend reads are sufficient — **no Room migration, no DAO change**:

- `TransactionDao.getAllNow()` (all active rows)
- `BudgetPeriodDao.getAllNow()` (all periods)
- `BudgetPeriodDao.getAllArchivedNow()` (all archived rows)

If later profiling shows the load to be heavy on very large histories, add a range query
(`SELECT * FROM transactions WHERE type='SPENT' AND date BETWEEN :from AND :to`) — planned as an
optional optimization, not required.

### Reusable building blocks already in the codebase

- `analytics/multiPeriodTotals()` + `MultiPeriodTrendCard` — month-over-month totals.
- `analytics/dailySpendTotals()` / `monthDayCounts()` — per-day + per-month buckets.
- `analytics/SpendsTrendCard` + `SpendsTrendAreaChart` (gradient area line, min/max markers,
  dashed avg line, tap-to-inspect) — the "hero" chart pattern.
- `analytics/SpendsWeekdayCard` — weekday distribution.
- `analytics/SpendsCalendar` — month heatmap with day drill-down.
- `analytics/categoriesChart/` (`SpendCategoriesCard`, `CategoriesChartCard`, `DonutChart`) —
  category donut + chips.
- `data/categories/categoryTotals()`, `categoryKey()`, `CategoryKey` — category aggregation.
- `settings/PeriodSummary.kt` (`buildPeriodSummary`) — per-period KPI summary.
- `base/AnimatedNumber` — rolling number animation.
- `base/TextRow` / `base/ButtonRow` — navigation rows.
- `ai/AiBackend.kt` — `callAi(context, systemPrompt, userPrompt)` + `resolveAiBackendConfig`,
  retry/backoff, `NotConfigured/Failure/Success`.
- `ui/` colors (`colorGood/colorNotGood/colorBad/colorMax/colorMin`), `harmonize()`, `toPalette()`.
- Typography: Manrope variable font (weights 600-900) via `ui/Typography.kt`.
- `util/` helpers: `numberFormat`, `prettyDate`, `countDays`, `roundToDay`, `toLocalDate`,
  `isZero`, `smoothPath`.

---

## The Pattern Engine (pure analytics core)

New package: `app/src/main/java/com/danilkinkin/buckwheat/patterns/`.

No Android, no DataStore, no Room imports — everything operates on plain value models so it runs
under Robolectric/JUnit on the desktop. All math uses `BigDecimal` with explicit `RoundingMode`
(scale-sensitive — see gotchas in MEMORY.md).

### Pure models (`PatternData.kt`)

```
data class PatternSpend(date: Date, value: BigDecimal, category: String?)   // view of a spend
data class PatternPeriod(start: Date, finish: Date, budget: BigDecimal, totalSpent: BigDecimal, isImported: Boolean)
data class PatternDataset(
    spends: List<PatternSpend>,        // all SPENT, active + archived, chronological
    periods: List<PatternPeriod>,      // archived periods + current period appended
    currencyCode: String,
    today: LocalDate,
)
data class CategoryPattern(name, key: CategoryKey, displayName, total, percent, monthlyAverage,
                           trend: TrendDirection, shareOfVariation, monthCount, activeMonths)
data class MonthlyPoint(label: String, spent: BigDecimal, budget: BigDecimal?, isCurrent: Boolean)
data class DayOfWeekPoint(day: DayOfWeek, total: BigDecimal, count: Int, sharePercent: Int)
data class Anomaly(date: LocalDate, amount: BigDecimal, category: String?,
                   expected: BigDecimal, threshold: BigDecimal, reason: AnomalyReason)
data class Forecast(projectedThisMonth: BigDecimal?, nextMonth: BigDecimal?,
                    monthlyAverage: BigDecimal, trendPercent: Int, pace: PaceLabel)
enum class TrendDirection { UP, DOWN, STABLE }
enum class AnomalyReason { ABOVE_3M_AVG, ABOVE_WEEKDAY_MEDIAN, ONE_OFF_BIG_TICKET }
```

### Metrics (pure functions in `PatternEngine.kt`)

0. **Name normalization / typo merging (applies to categories AND tags/comments)** — real user
   data is messy: "food", "Food ", "  FOOD  ", "food!!" are the same thing to a pattern engine.
   Before ANY grouping:
   - `normalizeName(raw): String` — `trim()` all edges, collapse runs of internal whitespace to
     a single space, lowercase (locale-insensitive), strip trailing/duplicate punctuation
     (`!`, `?`, `.`, `,`, `-`), and normalize unicode (NFKC) so curly quotes/accents match.
   - `mergeCategoryVariants(names): Map<String /*canonical key*/, String /*display name*/>` —
     group by `normalizeName`; then merge near-duplicate groups whose Levenshtein distance is
     <= 2 (catches "food" vs "foood" vs "foods"). The **display name keeps the most-used original
     spelling** the user typed (their typo is NOT shown back to them; the majority spelling wins).
   - `canonicalCategory(spend)` = persisted `category` → if blank, `offlineCategoryOrNull(comment)`
     → then mapped through `mergeCategoryVariants`. Every downstream metric groups by this key.
   - The recurring-charge detector also normalizes comments the same way so "Netflix",
     "netflix " and "Netflix " match as one subscription.
   - `CategoryPattern` carries `displayName` (majority spelling) so suggestions/AI text read
     naturally and match the app's category labels.

1. **Monthly trend** — `monthlyTotals(dataset): List<MonthlyPoint>` (reuses `multiPeriodTotals`
   logic concept but on `PatternDataset`, oldest-first, current period last and flagged).
   - `trendDirection(monthlyPoints, window = last 3)`: linear slope sign on last-3 points;
     `STABLE` when |slope| < 5% of average month.
   - `trendPercent(firstMonth, lastMonth)`: relative change, `HALF_EVEN`, scale 1.
2. **Category analysis** — `categoryPatterns(dataset): List<CategoryPattern>`:
   - per-category total, % of total spend, monthly average over active months (months with any
     spend; quiet months excluded from the denominator — same rule as `CategoryAutoAssign`).
   - per-category trend via first-half vs second-half split (growing / declining / stable).
   - concentration index = top-category share + Herfindahl (sum of squared shares) — "is spend
     spread or one big category?"
3. **Calendar rhythms** — `weekdayPatterns(dataset)`: total + count + share per weekday.
   - `weekendVsWeekdayDelta(...)`: (weekend avg/day - weekday avg/day), normalized to %.
   - `dayOfMonthPattern(dataset)`: group by day-of-month into payday buckets (1-5, 6-10, ...,
     26-31) to surface "first-of-month spikes".
   - `noSpendDays(dataset)` and `busiestDay(dataset)` (highest single-day total).
4. **Budget compliance** — `budgetCompliance(dataset)`:
   - per archived period: utilization % (`totalSpent/budget`), over/under flag, rank
     (best month / worst month).
   - count of overspent periods + `overspendDays` (reuse `overspendDayCount` logic per period,
     capped at today).
5. **Anomalies** — `findAnomalies(dataset): List<Anomaly>`:
   - spends > `max(2 * monthlyMedian, monthlyAverage)` on a single row, or
   - a day whose total > 2x the weekday median for that weekday (this catches weekend blowouts
     that are "normal" per-transaction but pattern-breaking per-day).
   - capped list (top 5 by excess amount) so the page stays scannable.
6. **Forecast** — `forecast(dataset, dailyBudget, today)`:
   - `monthlyAverage` (last 3 completed months), `trendPercent`.
   - `projectedThisMonth` = elapsed spend + (days-left avg); `nextMonth` = monthlyAverage *
     (1 + trend).
   - `paceLabel`: Over budget / At risk / On track / Saving (vs `dailyBudget` when set).
7. **Optimization suggestions** — `buildSuggestions(dataset, forecast): List<InsightSuggestion>`:
   ranked rule engine, each `(severity: HIGH/MEDIUM/LOW, title, body, categoryKey?, actionable)`:
   - Top category > 35% of total → "reduce by trimming <cat> to X/mo (avg for others)".
   - Category growing across halves → "fastest-growing category".
   - Weekend avg > 1.5x weekday avg → "weekend overspend" with the delta.
    - Recurring subscription signals (same **normalized** comment + similar amount >= 3 months
      apart — variant spellings/"Netflix " vs "netflix" count as the same charge) →
      "possible recurring charge" (reuses the spirit of the deleted interleaved engine's cadence
      idea, but as a read-only suggestion, no scheduling).
   - Overspent months > half of archived → "budget is set too tight / adjust budget".
   - Low diversification (Herfindahl > 0.35) → "spend concentrated in one category".
   - No-spend days ratio high → "you have X no-spend days; you are already disciplined".
8. **Offline narrative** — `buildPatternReport(dataset, suggestions, forecast): String` in the
   exact same shape as `buildOfflineReport` (overview line + "• " bullets + "Watch out for:" +
   "Tip:") so the AI/offline source badge swap is seamless.

### AI layer (`PatternPrompt.kt`)

- `buildPatternAiSystemPrompt()` — analyst persona, rules: language of the user, bullet structure,
  never invent transactions, currency-aware amounts, "keep under 160 words".
- `buildPatternAiUserPrompt(summary)` — the **anonymous** aggregate: totals per month, category
  shares (using the **normalized display names** so "Food" isn't split into "food"/"Food "),
  weekday deltas, budget compliance, anomalies (amounts + dates only, no comments),
  forecast, current top suggestions. Same privacy posture as `SpendInsightSummary` (no comment
  text sent — `PatternSpend` carries no comment at all).
- `parsePatternAiReport(raw)` — same cleanup as `parseAiInsightReport` (strip fences, echoed label,
  3+ newlines).
- `generatePatternAiInsight(context, summary)` — wraps `callAi`; returns
  `AiInsightResult.Success/Failure/NotConfigured` (reuse the existing sealed type in `ai/`).

---

## The new page: "Spending Patterns" (`PatternsSheet.kt`)

### Entry points (integration)

| Where | What |
|-------|------|
| `settings/Settings.kt` | New `TextRow` "Spending patterns" (`ic_analytics`, right arrow) above "Monthly report" → opens `PATTERN_INSIGHTS_SHEET` |
| `analytics/Analytics.kt` | New `ButtonRow` "Analyze patterns" near Export/Share (shows when spends exist) |
| `home/BottomSheets.kt` | Register `BottomSheetWrapper(name = PATTERN_INSIGHTS_SHEET)` |

`const val PATTERN_INSIGHTS_SHEET = "patternInsights"` lives in `PatternsSheet.kt`
(convention: sheet constants live beside the composable).

### ViewModel (`PatternsViewModel.kt`)

- `@HiltViewModel`, injects `SpendsRepository` + `GetCurrentDateUseCase` (same shape as
  `AiInsightViewModel`).
- `state: LiveData<PatternsUiState>`:
  ```
  sealed interface PatternsUiState {
      Idle | Loading
      data class Report(
          dataset: PatternDataset,
          window: PatternWindow,               // flexible month count (see below)
          metrics: PatternMetrics,             // every pure-engine result
          narrative: String, isAi: Boolean, aiFailure: String?, aiLoading: Boolean,
      )
      Error(message)
  }
  // Flexible window: ANY month count, not just preset values.
  data class PatternWindow(
      val months: Int,      // how many months back to analyze, 1..availableMonths
      val allData: Boolean, // true = ignore `months`, use every month available
  )
  ```
- `generate(window)` — guards duplicates; loads via repository flows, truncates `PatternSpend`
  list to the window (`PatternWindow(months = 6)` default), runs the pure engine on a background
  dispatcher, publishes the **offline** report instantly, then upgrades with AI in the background
  (identical `aiLoading` swap-in as `AiInsightViewModel.generate()`).
- `setWindow(window)` — re-runs engine only (no AI refire unless the report is AI-sourced and
  cheap; decision: keep the last AI text on window change to avoid surprise spend).

**Window UI** — a flexible selector, not fixed chips:
- `FilterChip` presets `3M / 6M / 12M / All` for one-tap, **plus** a `-` / `+` stepper (or a
  slider) to set an exact month count from 1 up to the months actually available in the data
  ("more or less", e.g. 4, 5, 7, 9…). The chosen count is shown as a label, e.g. "Last 9 months".
- Range is bounded by data: `availableMonths` = months spanned between the oldest spend and today
  (min 1). `All` = the full history the user has.

### Layout (from top to bottom)

```
┌────────────────────────────────────────────────┐
│  [TopographyBackground gradient header]        │  ← decorative contour lines + primary→tertiary
│    "Spending Patterns"  (titleLarge, weight 900)│     gradient, subtle animateFloatAsState drift
│    subtitle: "6 Aug 2025 - 16 Aug 2026 · 8 mo"  │
│    [3M] [6M] [12M] [All]  [- 9 mo +]  chips+   │  ← flexible window: presets + -/+ stepper
│        stepper row (1..available months)        │     for any month count (default 6M)
├────────────────────────────────────────────────┤
│  KPI hero row (AnimatedNumber)                 │  ← staggered fade-in
│    Total spent | Avg / month | Projected now    │
│  Trend chip: "▲ 12% vs previous 6 months"      │  ← colorGood/colorNotGood/colorBad
├────────────────────────────────────────────────┤
│  Card 1  Monthly trend — gradient area line    │  ← reuse SpendsTrendAreaChart pattern
│           + min/max markers + dashed budget    │     + tap-to-inspect day label
│           + month labels row                    │
├────────────────────────────────────────────────┤
│  Card 2  Where your money goes — donut + chips │  ← reuse CategoriesChartCard / SpendCategoriesCard
├────────────────────────────────────────────────┤
│  Card 3  Category trends — 3-col grid of mini  │  ← NEW small SparklineCanvas per top-4 category
│           area sparks (primary/surface colors)  │     (new Canvas, ~60dp tall)
├────────────────────────────────────────────────┤
│  Card 4  Weekday rhythm — bar chart            │  ← reuse SpendsWeekdayCard; weekend delta callout
├────────────────────────────────────────────────┤
│  Card 5  Day-of-month rhythm — heat bar row    │  ← NEW dayOfMonthPattern → 31 slim bars
│           1-5 | 6-10 | ... | 26-31 buckets     │
├────────────────────────────────────────────────┤
│  Card 6  Budget compliance — per-month bars    │  ← reuse MultiPeriodTrendCard; worst/best flags
├────────────────────────────────────────────────┤
│  Card 7  Anomalies — list rows with severity   │  ← colored dot + date + amount + expected
├────────────────────────────────────────────────┤
│  Card 8  What to optimize — ranked suggestion  │  ← HIGH/Medium/Low severity accent bar
│           cards (tap → drills into category)    │
├────────────────────────────────────────────────┤
│  Card 9  AI narrative + AI/Offline badge       │  ← reuse ReportNarrative/ReportBody pattern
│           [Regenerate]  · "Improving with AI…" │
│           · "AI unavailable (reason)"          │
└────────────────────────────────────────────────┘
```

### Design decisions (visuals, typography, animation)

- **Graphs / "graph images"**: reuse existing analytics chart composables where they fit
  (`SpendsTrendCard`, `MultiPeriodTrendCard`, `SpendsWeekdayCard`, `CategoriesChartCard`) so the
  page is instantly recognizable as Buckwheat. Two new small Canvas charts are added for pattern-
  specific views: `CategorySparklines` (per-category mini area lines) and `DayOfMonthBars`.
- **Topography**: new `patterns/TopographyBackground.kt` — a Canvas that draws 3-4 concentric /
  contour "topographic" loops (smooth, offset sine rings via `smoothPath`), extremely subtle
  (alpha 0.04-0.08, `primary`/`tertiary`), placed behind the header. Optional slow drift with
  `animateFloatAsState(rememberInfiniteTransition)` so the lines breathe without being busy.
  This is decoration only — zero data semantics.
- **Different fonts**: the app ships Manrope variable font with per-style weight variation
  (`ui/Typography.kt`). Use the full range for hierarchy:
  - Hero KPI numbers: `displayMedium` (weight 900) + `fontFeatureSettings("tnum")` for tabular
    alignment.
  - Page title: `titleLarge` (700) or `headlineSmall` (700); section headers `titleMedium` (700);
    captions `labelMedium/labelSmall` (700/600); narrative `bodyMedium` (700).
  - Optional stretch: bundle a lightweight mono font (e.g. a `.ttf` in `res/font/`) for the
    "tabular numbers" hero so KPI digits align — only if the user wants the stronger look;
    otherwise `fontVariationSettings` + `tnum` on Manrope is enough.
- **Animation**:
  - KPI numbers roll in via `base/AnimatedNumber`.
  - Sections reveal with staggered `AnimatedVisibility` (`fadeIn + slideInVertically(12.dp)`,
    150-250ms, staggered ~60ms) driven by a `LaunchedEffect` progress index.
  - `animateFloatAsState` on chart "draw-in" progress (line/path alpha or a clip fraction) for the
    two new Canvas charts.
  - `animateContentSize` on the anomaly/suggestion lists so expand/collapse is smooth.
  - Theme-driven colors only (no hard-coded hex) — `harmonize(colorMax/Min)` for markers,
    `colorGood/NotGood/Bad` for deltas and severities.

### Alignment / consistency rules (the app's design language)

- Follow the sheet header convention: centered `titleLarge` in a padded Box (like `AiInsightSheet`
  / `PastPeriodsSheet`), `Surface(Modifier.padding(top = LocalBottomSheetScrollState.current.topPadding))`.
- Horizontal padding `24.dp` inside cards, `16.dp` page gutter; cards use
  `MaterialTheme.shapes.extraLarge` (matches analytics).
- All money strings via `numberFormat(context, value, currency)` and dates via `prettyDate(...,
  shortMonth = true)` — locale-safe.
- Scroll the page with `verticalScroll(rememberScrollState())` (this is a long report page, not a
  LazyColumn; consistent with `AiInsightSheet`).
- Bottom padding = `navigationBarHeight` like other sheets.
- New strings live in `values/strings.xml` EN-only (convention: EN-only new strings, RU already
  validated as UTF-8).

---

## Implementation plan (phases)

Each phase ends with a green build; ship order is what the user values most first.

### Phase 1 — Engine + models + tests (pure core)
Files:
- `patterns/PatternData.kt` (new) — models above.
- `patterns/PatternEngine.kt` (new) — all pure metric functions + `buildSuggestions` +
  `buildPatternReport` + `forecast` + `findAnomalies`.
- `app/src/test/java/.../patterns/PatternEngineTest.kt` (new) — one suite per function family
  (normalization/typo-merge, trend, categories, weekday/day-of-month, compliance, anomalies,
  forecast, suggestions, report).
  Target ~40-50 cases, all deterministic (`LocalDate`-based fixtures, no `Date()` in expectations).
  Normalization suite covers: trimming, internal whitespace collapse, case folding, trailing
  punctuation, unicode (NFKC), Levenshtein <= 2 merges, majority-spelling display, blank/`OTHER`
  fallback, and tag-based recurring detection across variant spellings.

Verify: `:app:spotlessApply` + `:app:testDebugUnitTest`.

### Phase 2 — AI layer
Files:
- `patterns/PatternPrompt.kt` (new) — prompt builders + parser + `generatePatternAiInsight`.
- `app/src/test/java/.../patterns/PatternPromptTest.kt` (new) — prompt content, parser cleanup,
  `NotConfigured`/`Failure` mapping (pure parts).

### Phase 3 — ViewModel + data wiring
Files:
- `patterns/PatternsViewModel.kt` (new) — `PatternsUiState`, `generate(window)`, `setWindow`,
  dataset builder from `SpendsRepository` flows, background dispatch, AI upgrade.
- (No DI module change — `SpendsRepository` + `GetCurrentDateUseCase` are already injectable.)

### Phase 4 — Page UI + integration
Files:
- `patterns/PatternsSheet.kt` (new) — `PATTERN_INSIGHTS_SHEET` const, full layout, all section
  cards, window chips, KPI hero, suggestion cards, narrative card, `TopographyBackground` usage.
- `patterns/TopographyBackground.kt` (new) — contour Canvas.
- `patterns/CategorySparklines.kt` (new, or folded into `PatternsSheet.kt`) — mini trend sparks.
- `patterns/DayOfMonthBars.kt` (new, or folded) — 31-slim-bar rhythm chart.
- `home/BottomSheets.kt` — register the sheet wrapper.
- `settings/Settings.kt` — add the "Spending patterns" `TextRow`.
- `analytics/Analytics.kt` — add "Analyze patterns" `ButtonRow`.
- `values/strings.xml` — ~16 new EN strings (`patterns_title`, `patterns_subtitle_range`,
  `patterns_window_3m/6m/12m/all`, `patterns_window_stepper_dec/inc`, `patterns_window_last_months`
  ("Last %1$d months"), `patterns_total/avg_month/projected_now`, `patterns_trend_chip`,
  `patterns_monthly_trend`, `patterns_categories`, `patterns_category_trends`,
  `patterns_weekday`, `patterns_day_of_month`, `patterns_compliance`, `patterns_anomalies`,
  `patterns_optimize`, `patterns_ai_badge_*`, `patterns_regenerate`, `patterns_analyze_button`).

### Phase 5 — Polish + full verification
- Re-read the page against the alignment rules above; verify dark mode, small screens,
  RTL (canvas charts are LTR-safe by design — confirm layout direction like `SpendsChart` does).
- Golden pipeline: `:app:spotlessApply` + `:app:testDebugUnitTest` + `:app:assembleDebug`.
- Optional stretch (ask user): bundled mono font for hero digits; export/share the pattern report.

---

## File summary

| Action | File |
|--------|------|
| new | `patterns/PatternData.kt` |
| new | `patterns/PatternEngine.kt` |
| new | `patterns/PatternPrompt.kt` |
| new | `patterns/PatternsViewModel.kt` |
| new | `patterns/PatternsSheet.kt` |
| new | `patterns/TopographyBackground.kt` |
| new | `patterns/CategorySparklines.kt` |
| new | `patterns/DayOfMonthBars.kt` |
| new | `test/.../patterns/PatternEngineTest.kt` |
| new | `test/.../patterns/PatternPromptTest.kt` |
| edit | `home/BottomSheets.kt` (register sheet) |
| edit | `settings/Settings.kt` (entry row) |
| edit | `analytics/Analytics.kt` (entry button) |
| edit | `res/values/strings.xml` (~16 strings) |

No DB migration, no schema change, no new dependencies, no new permissions.

---

## Risks & open questions

- **Data completeness**: patterns are only as good as history. Periods created before the category
  column (pre-DB-v14) have `category = null` → the engine's offline keyword categorizer
  (`offlineCategoryOrNull`) should be applied at load time to `null` categories so pattern
  category views are still meaningful (existing behavior in analytics). Run the category
  assignment scheduler once at page open if `uncategorizedCount > 0` (already how Analytics works).
- **Currency mixing**: archived periods store their own `currencyCode`; if the user switched
  currencies, monthly totals mix units. Mitigation: pattern math uses raw values and the page
  shows the current currency; a warning chip appears when `periods.map{currencyCode}.distinct().size > 1`.
- **AI privacy**: never send comments; `PatternSpend` intentionally has no comment field.
- **Performance**: full-history scan is fine for 6-8 months (< a few thousand rows). Range query
  is the documented escape hatch if it ever grows.
- **AI cost on regenerate**: `setWindow` keeps the last AI text (no refire) — decided; revisit if
  the user wants window-specific AI text.
- **"Too many cards" risk**: the page is long. Mitigation: sections that are empty/irrelevant
  (e.g. no anomalies) are omitted entirely, and suggestion cards are capped at 5.
- **Name-merge side effects**: `mergeCategoryVariants` must only merge by normalized similarity
  within reason (distance <= 2) so genuinely different categories ("food" vs "food court") are
  never collapsed. The merge affects pattern VIEWS only — it never renames the user's saved
  categories or rewrites transactions; if the user later wants to physically merge/rename
  categories, that stays a separate Categories-management feature.

## Out of scope
- Writing new data (no schedules, no caps, no budget changes — the page is read-only analysis;
  the only "write" is optional category assignment for null categories, same as Analytics).
- Editing AI settings from the page (deep-link only to `VOICE_AI_SETTINGS_SHEET`, same as the
  monthly report's NotConfigured path).
- Interleaved-budget-style auto-scheduling (deleted feature; the cadence suggestion is read-only).
- Sharing/export of the pattern report (stretch; reuse `ShareSummary` pattern if requested).
