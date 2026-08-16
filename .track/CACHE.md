# Cache — Quick Reference

> **Security rules for all agents: see `.track/SECURITY.md`** (binding). No hardcoded external links/emails; API key excluded from all backups; `exported` stays false unless the OS requires it; no analytics SDKs.

## Build Commands
```powershell
# Quick compile check (faster than full build)
.\gradlew.bat :app:compileDebugKotlin

# Full debug build (always run this after changes)
.\gradlew.bat assembleDebug

# Clean build
.\gradlew.bat clean assembleDebug

# Run tests
.\gradlew.bat testDebug

# Lint
.\gradlew.bat lintDebug

# Spotless format check
.\gradlew.bat spotlessCheck
```

## Gradle on this machine (IMPORTANT — flaky daemons)
- **Full builds are SLOW (~30 min on a cold daemon)** — `compileDebugKotlin` alone can take 15+ min when the Kotlin compile daemon has to start cold.
- The opencode shell tool **kills the process tree when a command times out**, which often takes the Gradle daemon down mid-compile. A killed-but-alive daemon left in "busy" state will make later `gradlew` invocations hang or start yet another daemon ("1 busy ... could not be reused").
- **Workaround**: run the build **detached** and poll the log instead of blocking:
  ```powershell
  $log = "$env:TEMP\buckwheat_gradle_out.log"
  Remove-Item $log -ErrorAction SilentlyContinue
   $inner = "gradlew.bat :app:spotlessApply :app:assembleDebug :app:testDebugUnitTest --tests `"com.danilkinkin.buckwheat.patterns.*`" > `"$log`" 2>&1"
   $p = Start-Process -FilePath "cmd.exe" -ArgumentList '/c',$inner -WorkingDirectory "D:\Just-try\buckwheat" -PassThru
   $p.Id | Out-File "$env:TEMP\buckwheat_gradle.pid"
   ```
   **Robust variant (used 2026-08-16, Phase 4)**: write the inner command to a `.cmd` file first, then run it — avoids quoting fallout entirely:
   ```powershell
   $cmd = "$env:TEMP\buckwheat_gradle.cmd"
   Set-Content -Path $cmd -Value "gradlew.bat :app:spotlessApply :app:assembleDebug :app:testDebugUnitTest --tests `"com.danilkinkin.buckwheat.patterns.*`" > `"$log`" 2>&1" -Encoding ASCII
   $p = Start-Process -FilePath "cmd.exe" -ArgumentList @('/c', "`"$cmd`"") -WorkingDirectory "D:\Just-try\buckwheat" -PassThru -WindowStyle Hidden
   $p.Id | Out-File "$env:TEMP\buckwheat_gradle.pid"
  ```
  then poll `Get-Content $log -Tail 4` every ~100 s until `BUILD SUCCESSFUL`/`FAILED`. The detached daemon survives tool-call kills.
- **Never run two detached builds at once** (they spawn competing daemons that fight over locks). If a run stalls, `Get-CimInstance Win32_Process -Filter "Name='java.exe'"` → `Get-Process java | Stop-Process -Force` to reset.
- **Avoid** `Start-Process -RedirectStandardOutput` with a `.bat` — it hangs. Use `cmd /c "gradlew.bat ... > log 2>&1"` instead.
- Test results XML: `app\build\test-results\testDebugUnitTest\*.xml` (per-suite `tests=`/`failures=`).

## Key File Paths (Upstream Master)
| Purpose | Path |
|---------|------|
| Main Activity | `app/.../MainActivity.kt` |
| App ViewModel | `app/.../data/AppViewModel.kt` |
| Spends ViewModel | `app/.../data/SpendsViewModel.kt` |
| Editor ViewModel | `app/.../editor/EditorViewModel.kt` |
| Main Repository | `app/.../di/SpendsRepository.kt` |
| Settings Repository | `app/.../di/SettingsRepository.kt` |
| Room Database | `app/.../di/DatabaseModule.kt` |
| Hilt Module | `app/.../di/AppModule.kt` |
| Transaction DAO | `app/.../data/dao/TransactionDao.kt` |
| Storage DAO | `app/.../data/dao/StorageDao.kt` |
| Keyboard | `app/.../keyboard/Keyboard.kt` |
| Voice Input Parser | `app/.../keyboard/VoiceInputParser.kt` |
| Voice AI Parser | `app/.../keyboard/VoiceAi.kt` |
| Voice AI Settings Sheet | `app/.../settings/VoiceAiSettingsSheet.kt` |
| Voice Widget Commit Service | `app/.../widget/voice/VoiceWidgetCommitService.kt` |
| Voice Widget Design Setting | `app/.../settings/VoiceWidgetDesignSetting.kt` |
| Category Cap Logic | `app/.../data/categories/CategoryCap.kt` |
| Category Cap Notifier | `app/.../notifications/CategoryCapNotifier.kt` |
| Category Caps ViewModel | `app/.../settings/CategoryCapsViewModel.kt` |
| Category Caps Sheet | `app/.../settings/CategoryCapsSheet.kt` |
| Spend Categories Card | `app/.../analytics/categoriesChart/SpendCategoriesCard.kt` |
| Donut chart | `app/.../analytics/categoriesChart/DonutChart.kt` — pure `internal donutItemAngles(items)` (empty → `emptyList()`, zero-total → equal 360/n slices, tiny slices padded to 28° with redistribution) + `DonutChart` composable that early-returns on empty items (since 2026-08-15; `reduce` on an empty list used to throw `UnsupportedOperationException` and crash the Monthly report). Tests: `app/src/test/.../analytics/categoriesChart/DonutChartTest.kt` (5) |
| Date picker bounds vs shown months | `app/.../base/datePicker/model/CalendarState.kt` — the ENABLED range comes from `disableBeforeDate`/`disableAfterDate` (`CalendarUiState.disabledBefore/disabledAfter` → `Day` disabled → non-clickable); the SHOWN month range (`listMonths`) comes from `showBeforeDate`/`showAfterDate`, which default to the disable bounds. So display and enable can differ (editor since `fb6b619`). Editor `DatePickerDialog` (`editor/dateTimeEdit/DateEditor.kt` + `DateTimeEditPill.kt`): `disableBeforeDate = startPeriodDate`, `disableAfterDate = today`, `showBeforeDate = startPeriodDate`, `showAfterDate = finishPeriodDate` → calendar renders the period months, days before the period start / after today are greyed + unclickable. No period → `showBeforeDate`/`disableBeforeDate` null → `calendarStartDate` (one year back) renders, only future dates blocked. `FinishDateSelector`/`SpendsCalendar` unaffected (they set explicit bounds). PITFALL chain: `CalendarState` → `CalendarUiState.isDisabledDay` → rendered months (LESSONS Issue 28 + amendment) |
| Smooth curve helper | `app/.../util/chart.kt` — `smoothPath(points)` (Catmull-Rom → cubic Bézier) + pure `internal smoothSegments(points)` returning the clamped control points. Since 2026-08-15 control-point y is clamped into the data's `[minY, maxY]` (a cubic Bézier lives inside its control-point hull → the curve never overshoots the data extent). Consumed by `analytics/SpendsTrendCard.kt`, `analytics/MultiPeriodTrendCard.kt`, `settings/PeriodSummaryCard.kt`. Tests: `app/src/test/.../util/SmoothPathTest.kt` (4) |
| Area-chart vertical band | All three smooth area-line charts plot inside a vertical band so nothing clips at the edges (since 2026-08-15): `SpendsTrendAreaChart` (`analytics/SpendsTrendCard.kt`, used in Analytics + Monthly report) and `MultiPeriodTrendChart` (`analytics/MultiPeriodTrendCard.kt`) use `topInset = 12.dp`/`bottomInset = 6.dp`; `ExpenditureAreaChart` (`settings/PeriodSummaryCard.kt`) already had 8dp insets — the `smoothPath` clamp fixed its curve overshoot |
| Spending Pattern Engine (new 2026-08-16, Phase 1) | `app/.../patterns/PatternData.kt` (pure models: `PatternSpend` = date/value/category, deliberately NO comment — AI-privacy; `PatternCharge` = comment-carrying, recurring-detector only) + `app/.../patterns/PatternEngine.kt` (pure engine: `normalizeName`, `categoryNameIndex`/`mergeCategoryVariants` Levenshtein≤2, `monthlyTotals`, `trendDirection`, `categoryPatterns`, `concentrationIndex`, `weekdayPatterns`, `weekendVsWeekdayDelta`, `dayOfMonthPattern`, `noSpendDays`, `budgetCompliance`, `findAnomalies`, `forecast`, `recurringCharges`, `buildSuggestions`, `buildPatternReport`, `analyzePatterns`, plus `categoryMonthlySeries`/`CategoryMonthlySeries`) + `app/src/test/.../patterns/PatternEngineTest.kt` (64). Plan: `.track/SPENDING_PATTERNS_PLAN.md` (5 phases; ALL DONE 2026-08-16). NOTE: full `testDebugUnitTest` still has 3 pre-existing failures (`RecurringChargeConfirmSheetTest`/`RecurringPaymentsSheetTest` — Robolectric `ComposeTimeoutException`, predates Phase 1) |
| Spending Patterns AI layer (new 2026-08-16, Phase 2) | `app/.../patterns/PatternPrompt.kt` (`buildPatternAiSystemPrompt`/`buildPatternAiUserPrompt`/`parsePatternAiReport`/`mapPatternAiResult`/`generatePatternAiInsight` + `PatternAiSummary`/`buildPatternAiSummary(dataset, metrics)`) + `app/src/test/.../patterns/PatternPromptTest.kt` (12). **Privacy**: recurring-charge suggestions stripped from the AI summary (their body embeds comment-derived names) — only the recurring COUNT is sent; `PatternSpend` carries no comment so nothing comment-derived leaks |
| Spending Patterns ViewModel (new 2026-08-16, Phase 3) | `app/.../patterns/PatternsViewModel.kt` (`@HiltViewModel`; `PatternsUiState{Idle|Loading|Report(dataset, window, availableMonths, metrics, narrative, isAi, aiFailure, aiLoading)|Error}`; `generate(window)` offline-first + background AI upgrade with stale-result guard; `setWindow` engine-only, keeps last AI narrative) + window helpers in `PatternEngine.kt` (`availableMonths`, `windowStartDate`, `applyWindow` — periods windowed too, deviation from plan) + `app/src/test/.../patterns/PatternsWindowTest.kt` (11). Dataset builder: current + archived SPENT (`toTransaction`), category = stored else offline keyword guess, periods = DB + constructed current period, comment-carrying charges kept out of the dataset |
| Spending Patterns page (new 2026-08-16, Phase 4) | `patterns/PatternsSheet.kt` — `const val PATTERN_INSIGHTS_SHEET = "patterns"` (plan text said "patternInsights" — actual code uses "patterns"); header `TopographyBackground` (176dp) + window selector (`FilterChip` 3M/6M/12M/All + `− N +` stepper 1..`availableMonths`, plural `patterns_window_last_months`, "All time") + KPI hero (`AnimatedNumber`, trend pill `▲/▼ %d%%`/Stable) + `MultiPeriodTrendCard` + `SpendCategoriesCard` (tap → `CATEGORY_HISTORY_SHEET`) + `CategorySparklines`/`MiniSpark` + `SpendsWeekdayCard` + weekend-delta callout + `DayOfMonthBars` (31 bars + buckets + busiest-day caption) + `ComplianceCard` + `AnomaliesCard` + `SuggestionsCard` (severity accent bar; NO tap drill-in — canonical key doesn't map to a persisted `CategoryKey`, recorded deviation) + `NarrativeCard`/`ReportBody`. Also `patterns/TopographyBackground.kt` (contour Canvas, wobbled rings alpha 0.05–0.08, 12s drift). Wiring: `home/BottomSheets.kt` registers the sheet; `settings/Settings.kt` TextRow (`ic_equalizer`); `analytics/Analytics.kt` ButtonRow (before Export CSV). ~40 `patterns_*` strings (not 16) incl. a `plurals`. Build gotchas hit (fixed): `MultiPeriodPoint` fields are `spent`/`budget`/`isCurrent` NOT `value`/`isCurrentMonth`; read `MaterialTheme.colorScheme` OUTSIDE Canvas draw lambdas; `dp` needs `import androidx.compose.ui.unit.dp` inside DrawScope. Verified detached 2026-08-16: spotlessApply + assembleDebug green, patterns suites 87/87 |
| Category Battery Chip | `app/.../analytics/categoriesChart/CategoryBatteryChip.kt` (new 2026-08-12; battery pill for capped categories — fill = used up to cap via `categoryBatteryFraction`, label overlaid via `drawWithContent` + `clipRect`, amber ≥80%, red ≥100%) |
| Category Caps Test | `app/.../test/.../data/categories/CategoryCapsTest.kt` |
| Category Assigner | `app/.../data/categories/CategoryAssigner.kt` (new 2026-08-13; offline-first then AI over transactions + archived_transactions, persisted via DAO `updateCategory`) |
| Category Assignment Scheduler | `app/.../data/categories/CategoryAssignmentScheduler.kt` (new 2026-08-13; app-scoped `SupervisorJob() + Dispatchers.Default`, `AtomicBoolean`-coalesced `schedule()`, dirty-flag rescan, `StateFlow isRunning`, re-arm in `finally`) |
| Auto-categorize button (AI settings) | `app/.../settings/VoiceAiSettingsSheet.kt` → `CategoryAutoAssignSetting` (new 2026-08-13; `ic_label` icon + **Categorize** button → `SpendCategoriesViewModel.categorizeUncategorized()`; disabled + "Categorizing…" + indeterminate bar while running; status "All spends categorized"/"N spends need a category" from `uncategorizedCount`) |
| Uncategorized-count queries | `TransactionDao.getUncategorizedCount()` + `BudgetPeriodDao.getArchivedUncategorizedCount()` (`LiveData<Int>`, `SPENT AND (category IS NULL OR category = '')`); summed in `SpendCategoriesViewModel.uncategorizedCount` via `MediatorLiveData` |
| AI Insight Spend View | `app/.../ai/WindowSpend.kt` (pure `(date, value, category)` view fed to the AI math; moved out of the deleted `interleaved/` package 2026-08-13) |
| AI Insight (report engine) | `app/.../ai/AiInsight.kt` — pure `buildAiInsightUserPrompt`/`buildOfflineReport`/`overspendDayCount`/`parseAiInsightReport` + `generateAiInsight` (HTTP). `buildOfflineReport` (new 2026-08-13) = offline narrative engine, same bullet shape as AI output, deterministic dates via `INSIGHT_DATE_FORMAT` (**`prettyDate` is `@Composable` — never call from pure code**). Tests: `app/src/test/.../ai/AiInsightTest.kt` (18) |
| AI Insight ViewModel | `app/.../ai/AiInsightViewModel.kt` — `AiInsightUiState` (Idle/Loading/Report(data,text,isAi,aiFailure,aiLoading)/Error); `generate()` shows the offline report instantly then upgrades with AI in background; `buildOverviewData()` → `MonthOverviewData` |
| AI Backend Engine | `app/.../ai/AiBackend.kt` — single configurable backend (URL+key+model), `callAi`/`AiRouterResult` contract, `postBackend` hardened 2026-08-14 (explicit `connect()`, `StandardCharsets.UTF_8` writer+reader, `useCaches=false`). `resolveAiBackendConfig` requires URL + key; **model is OPTIONAL since `6e9a23a`** (blank → omitted from body, provider default used). `chatCompletionsUrl`/`modelsEndpoint` derive paths from base (`aiProviderBaseUrl` helper since `32d4b63`); `buildRequestBody` only puts `"model"` when non-blank; `extractProviderText` tolerates top-level `content` or `choices[0].message.content`; `cleanAiOutput` strips fences/`<think>`/html. Tests: `app/src/test/.../ai/AiBackendTest.kt` (15) |
| Month overview data | `app/.../ai/MonthOverviewData.kt` (new 2026-08-13; summary + spends + transactions + periods + start/finish dates + `ExtendCurrency`) |
| Monthly report sheet | `app/.../settings/AiInsightSheet.kt` — `AI_INSIGHT_SHEET`; auto-generates on open; budget-status card + analytics chart images (`SpendsTrendCard`/`SpendCategoriesCard`/`CategoriesChartCard`/`SpendsWeekdayCard`/`SpendsCalendar` heatmap with day drill-down to `VIEWER_HISTORY_SHEET`) + narrative card with AI/Offline badge |
| On-Track Alert Scheduler | `app/.../notifications/OnTrackAlertScheduler.kt` (`setWindow`, re-armed by receiver) |
| Past-Periods Sheets | `app/.../settings/PastPeriodsSheet.kt` (list — PR-157-style progress cards: date range + budget/spent + 8dp rest-budget bar + "X% of budget"/"Over by" status, imported keep spent+tag) + `PeriodDetailSheet.kt` (detail incl. analytics cards + spent/rest progress bar; date chip opens `FINISH_DATE_SELECTOR_SHEET` to edit period dates via `ArchivesViewModel.updatePeriodDates` → `BudgetPeriodDao.updateDates` since `15e6e24`) + `ArchivesViewModel.kt` |
| Past-period summary card | `app/.../settings/PeriodSummaryCard.kt` (curved area-line chart for month expenditure + 3×2 stat grid as of 2026-08-14) |

--- 
| Midnight Widget Refresh Scheduler | `app/.../widget/WidgetRefreshScheduler.kt` |
| Midnight Widget Refresh Receiver | `app/.../widget/WidgetRefreshReceiver.kt` |
| Recalc Budget VM | `app/.../recalcBudget/RecalcBudgetViewModel.kt` |
| Voice Parser Tests | `app/src/test/java/.../keyboard/VoiceInputParserTest.kt` |
| Budget Constructor | `app/.../wallet/BudgetConstructor.kt` |
| Wallet | `app/.../wallet/Wallet.kt` |
| Bottom Sheets | `app/.../home/BottomSheets.kt` |
| Theme | `app/.../ui/Theme.kt` |
| Locale | `app/.../ui/Locale.kt` |
| Export CSV | `app/.../wallet/rememberExportCSV.kt` |
| Currency Editor | `app/.../wallet/CurrencyEditor.kt` |
| Goals Sheet | `app/.../settings/GoalsSheet.kt` |
| Goals VM | `app/.../settings/GoalsViewModel.kt` |
| Recurring Payments Sheet | `app/.../settings/RecurringPaymentsSheet.kt` |
| Recurring Payments VM | `app/.../settings/RecurringPaymentsViewModel.kt` |
| Tags Management Sheet | `app/.../settings/TagsManagementSheet.kt` |
| Tags Management VM | `app/.../settings/TagsManagementViewModel.kt` |
| SavingsGoal DAO | `app/.../data/dao/SavingsGoalDao.kt` |
| Recurring DAO | `app/.../data/dao/RecurringDao.kt` |
| BudgetPeriod DAO | `app/.../data/dao/BudgetPeriodDao.kt` |
| RecurringTemplate entity | `app/.../data/entities/RecurringTemplate.kt` |
| Recurring due alerts scheduler | `app/.../notifications/RecurringPaymentAlertScheduler.kt` |
| Recurring due alerts receiver | `app/.../notifications/RecurringPaymentAlertReceiver.kt` |
| Recurring due text builder | `app/.../notifications/RecurringDueContent.kt` |
| Recurring alert settings UI | `app/.../settings/RecurringPaymentAlertSetting.kt` |
| Recurring due content tests | `app/src/test/java/.../notifications/RecurringDueContentTest.kt` |
| Goal progress nudge logic | `app/.../settings/GoalProgressNudge.kt` |
| Goal progress notifier | `app/.../notifications/GoalProgressNotifier.kt` |
| Goal nudge tests | `app/src/test/java/.../notifications/GoalProgressNudgeTest.kt` |
| Spend digest content builder | `app/.../notifications/SpendDigestContent.kt` |
| Spend digest scheduler | `app/.../notifications/SpendDigestScheduler.kt` |
| Spend digest receiver | `app/.../notifications/SpendDigestReceiver.kt` |
| Spend digest settings UI | `app/.../settings/SpendDigestSetting.kt` |
| Spend digest content tests | `app/src/test/java/.../notifications/SpendDigestContentTest.kt` |
| Category drill-down helper | `app/.../data/categories/SpendCategorizer.kt` (`transactionMatchesCategory`) |
| Category drill-down sheet | `app/.../analytics/ViewerHistory.kt` (`CATEGORY_HISTORY_SHEET` const + `onlyCategoryKey`) |
| Category chips (tappable) | `app/.../analytics/categoriesChart/TagAmount.kt` + `SpendCategoriesCard.kt` (`onCategoryClick`) |
| Voice widget per-instance config activity | `app/.../widget/voice/VoiceWidgetConfigureActivity.kt` (new) |
| Effective voice-widget design helper | `app/.../widget/voice/VoiceWidget.kt` (`effectiveVoiceWidgetDesign`) |
| Voice widget per-instance override key | `app/.../widget/CommonWidgetReceiver.kt` (`voiceDesignOverridePreferenceKey`) |
| Voice widget config tests | `app/src/test/java/.../widget/voice/VoiceWidgetConfigTest.kt` (new) |
| Category widget | `app/.../widget/category/` (new 2026-08-15): `CategoryWidget.kt` (`CategoryWidgetDesign { BATTERY, COMPACT }`, pure `effectiveCategoryWidgetDesign(override, global)`, Responsive 220×110/160/240/400 → ≤2/3/5/all pills, `PILL_HEIGHT_DP=24f`, `CONTENT_WIDTH_DP=204f`, bitmap pill `drawCategoryPillBitmap` with `canvas.clipRect` fill — Glance 1.1.1 has no Canvas composables; **background = `GlanceTheme.colors.background.getColor(context)` via `GlanceModifier.background(...)` since 2026-08-16** — was `appWidgetBackground()` which is invisible on many hosts), `CategoryWidgetData.kt` (pure `categoryWidgetRows`/`categoryWidgetPills`/JSON `serialize`/`parse` tolerant, `CATEGORY_WIDGET_PALETTE_SIZE = 7`), `CategoryWidgetReceiver.kt` (`@AndroidEntryPoint : WidgetReceiver`), `CategoryWidgetConfigureActivity.kt` (per-instance override via `categoryDesignOverridePreferenceKey`, "Follow global" clears it) |
| SavingsGoal entity | `app/.../data/entities/SavingsGoal.kt` |
| BudgetPeriod entity | `app/.../data/entities/BudgetPeriod.kt` |
| ArchivedTransaction entity | `app/.../data/entities/ArchivedTransaction.kt` |
| Daily reminder scheduler | `app/.../notifications/DailyBudgetReminderScheduler.kt` |
| Daily reminder receiver | `app/.../notifications/DailyBudgetReminderReceiver.kt` |
| Reminder message builder | `app/.../notifications/DailyReminderContent.kt` |
| Boot reschedule receiver | `app/.../notifications/ReminderBootReceiver.kt` |
| Reminder settings UI | `app/.../settings/DailyBudgetReminderSetting.kt` |
| Reminder content tests | `app/src/test/java/.../notifications/DailyReminderContentTest.kt` |
| Backup codec | `app/.../backup/BackupData.kt` |
| Backup repository | `app/.../di/BackupRepository.kt` |
| Backup/restore UI | `app/.../settings/BackupRestoreSetting.kt` |
| Backup/restore VM | `app/.../settings/BackupRestoreViewModel.kt` |
| Backup codec tests | `app/src/test/java/.../backup/BackupDataTest.kt` |
| Backup repo tests | `app/src/test/java/.../di/BackupRepositoryTest.kt` |
| Share summary | `app/.../analytics/ShareSummary.kt` |
| Share summary tests | `app/src/test/java/.../analytics/ShareSummaryTest.kt` |
| Category selector (editor) | `app/.../editor/category/CategorySelector.kt` |
| SpendCategory enum | `app/.../data/categories/SpendCategory.kt` |
| Spend Categorizer (offline + AI) | `app/.../data/categories/SpendCategorizer.kt` |
| Categories VM | `app/.../data/categories/SpendCategoriesViewModel.kt` |
| Categories Card (analytics) | `app/.../analytics/categoriesChart/SpendCategoriesCard.kt` |
| Monthly trend card (analytics) | `app/.../analytics/SpendsTrendCard.kt` |
| Monthly trend tests | `app/src/test/java/.../analytics/SpendsTrendTest.kt` |
| Smooth chart utility | `app/.../util/chart.kt` (new 2026-08-14; pure `smoothPath(points)` — Catmull-Rom → cubic Bézier with phantom endpoint control points, shared by `SpendsTrendCard`, `PeriodSummaryCard`, `MultiPeriodTrendCard`) |
| Weekday breakdown card (analytics) | `app/.../analytics/SpendsWeekdayCard.kt` |
| Weekday breakdown tests | `app/src/test/java/.../analytics/SpendsWeekdayTest.kt` |
| Compare-to-last-period card | `app/.../analytics/CompareToLastPeriodCard.kt` |
| Compare-to-last-period tests | `app/src/test/java/.../analytics/CompareToLastPeriodTest.kt` |
| Multi-period trend card | `app/.../analytics/MultiPeriodTrendCard.kt` (curved line chart as of 2026-08-14: smooth `smoothPath` spent line + dashed budget line + gradient area fill + color dots + tap-to-select) |
| Multi-period trend tests | `app/src/test/java/.../analytics/MultiPeriodTrendTest.kt` |
| History screen | `app/.../history/History.kt` — `composeHistoryRows` (private) walks `periodSpends` (+ archived only when searching) sorted by date ascending, emitting `DayDivider` → `Spent*` rows → `DayTotal` per day, then `reversed()` → newest day first (day-card grouping from the 2026-08-15 redesign REVERTED 2026-08-16, commit `b581ab1`; chart/donut/editor fixes from `a0883e2` kept). `onlyDay`/`onlyCategoryKey` filters + archived-rows-only-when-searching preserved; tap-edit + long-press copy/edit/delete via `SpentItemActions` |
| History list animation | `app/.../history/ListAnimation.kt` — `RowEntityType { DayDivider, Spent, DayTotal }` per-row entities (restored 2026-08-16; the day-card `RowEntity` keyed `"day-<date>"` is gone). `updateAnimatedItemsState` composites by key (reuse in place on content-hash match, fresh instance otherwise, removed rows exit at their old spot), LazyColumn keyed by per-instance `AnimatedItem.id`. Tests: `app/src/test/.../history/ListAnimationTest.kt` (4, reverted to the per-row model) |
| History day divider | `app/.../history/HistoryDateDivider.kt` (restored 2026-08-16) — date header between days |
| History day total | `app/.../history/TotalPerDay.kt` (restored 2026-08-16) — "Day total" row after each day's spends |
| History spent row | `app/.../history/SpentItem.kt` (restored 2026-08-16) — amount + date row, category pill (right-aligned), comment; **bottom padding `18.dp` = the "a bit more space" ask** (was 14dp pre-redesign). Used by both swipe (`SpentItemActions`) and read-only paths |
| History row actions | `app/.../history/SpentItemActions.kt` — tap = edit, long-press = copy/edit/delete `DropdownMenu` (`buildSpendCopyText` pure helper); renders `SpentItem`. `history/NoSpends.kt` empty state kept. `SwipeActions` is again the direct LazyColumn item root (Issue 34 crash structurally moot). **DELETED 2026-08-16 (redesign files)**: `DayCard.kt`, `TimelineRow.kt`; deleted 2026-08-15→restored 2026-08-16: `SpentItem.kt`, `HistoryDateDivider.kt`, `TotalPerDay.kt` |
| History grouping/hash tests | DELETED 2026-08-16 with the redesign: `app/src/test/.../history/HistoryRowsTest.kt` (the day-card grouping/content-hash model no longer exists) |
| Overspending notifier | `app/.../notifications/OverspendingNotifier.kt` |
| Overspend notification setting | `app/.../settings/OverspendNotificationSetting.kt` |
| Emoji picker | `app/.../base/EmojiPicker.kt` |
| SavedCategory entity | `app/.../data/entities/SavedCategory.kt` |
| SavedCategory DAO | `app/.../data/dao/SavedCategoryDao.kt` |
| Categories management sheet/VM | `app/.../settings/CategoriesManagementSheet.kt` / `.../CategoriesManagementViewModel.kt` |
| Categorizer Tests | `app/src/test/java/.../data/categories/SpendCategorizerTest.kt` |
| Manifest | `app/.../AndroidManifest.xml` |
| Gradle (app) | `app/build.gradle.kts` |
| Gradle (root) | `build.gradle.kts` |

## Emulator UI Automation (API 36, 1280×2856)
- Editor sheet top edge ~y=1552 when collapsed; swipes crossing it re-expand the sheet (wallet scroll is fragile)
- **Reliable route to Analytics**: toggle debug via keyboard `"0"`×8, then `"."`, then the apply column (bottom-right vertical button, bounds ~[957,2010][1220,2772]) → snackbar "Debug ON" (persisted in settings DataStore `debug` key); relaunch app, debug icon appears top-left of editor ([33,189][177,333]); tap it → DebugMenu → "Open period summary screen" → Analytics (with the full-month `SpendsCalendar`)
- Keyboard key bounds (1280×2856): 0=[60,2542][622,2772], .=[658,2542][921,2772], apply/delete column=[957,2010][1220,2772]
- `uiautomator dump` can transiently fail with "null root node" — retry after 1s; text assertions limited since most Compose elements expose no text
- **Screenshot capture (PowerShell)**: PowerShell `>` redirection and `[byte[]]` casts corrupt the PNG ("Parameter is not valid") — use `cmd /c "\"<adb>\" exec-out screencap -p > \"<file>\""` and read it with System.Drawing. The image/read tools only say "Image read successfully" (model can't see pixels) — verify by structured `GetPixel` scans (dump non-background runs per row, e.g. pill surface `238,240,255` = background).
- **Placing a widget on the home screen via adb/uiautomator**: KEYCODE_HOME → long-press empty space (swipe 640 1200 640 1200 800) → tap "Widgets" → tap the app row → the drag MUST start on the widget **preview image** (not its text label, which sits ~below it): `input motionevent DOWN x y` → wait ~1.5s (long-press detach; the picker closes and the home screen appears) → several `MOVE` steps → `UP`. `input draganddrop` scrolls the picker instead of detaching — use motionevent staging. Verify binding: `dumpsys appwidget` (host=`nexuslauncher`, provider=`VoiceWidgetReceiver`). Force a re-render: `adb shell am broadcast -a updateAction -n com.danilkinkin.buckwheat/.widget.voice.VoiceWidgetReceiver`. Glance paints text as bitmaps, so uiautomator sees no text; pixel-sample the widget band (PowerShell System.Drawing) for content colors. Watch `logcat -s VoiceWidget` / `E GlanceAppWidget: Error` for composition failures. **On-device E2E is the authoritative widget check — Robolectric `widget.update()` never runs `provideGlance` (false-green).**
- **Voice widget chart E2E (API 36, 1280×2856)**: band `x[344..1202] y[572..902]`; the theme primary renders blue `73,93,146` on this emulator (dynamic color). Chart verified by scanning x[398..1039]: goal-guard line (onSurfaceVariant 55%) at y≈751 full width; bars at equal slot centers — e.g. a 3-day period (clamped window) shows bars at x≈505/721/933 with `combineColors` blue→red and today's zero bar as a 2.dp onSurface-18% stub; amount text (18sp primary) ~y700, "For today" caption ~y650, min/max captions ~y826-844, bottom-left blue wedge = wallpaper through the 48.dp rounded corner (not a bug).
- **Voice widget layout (committed `75e12c2`, big-percent design)**: caption "Left today" (10sp gray) → hero = **% of daily budget remaining** (primary, 18sp bold) → "of ₹X daily" (9sp gray, string `voice_widget_of_daily`) → 3dp rounded progress bar → 26dp min/max-spike chart → "Budget ₹X · Spent ₹Y · Left ₹Z" summary (9sp gray). Percent + bar fraction = `dailyRemaining / dailyBudget` where `dailyRemaining = (dailyBudget − series.last()).coerceAtLeast(0)`. On-device (API 36, 5×2): caption y622-642, hero y672-702, sub y732-764, bar y772-778 (fill to x1039 = full width when today's spend is 0), chart y791-866 (red max ring ≈(398,791), blue min ring ≈(713,842), today white dot ≈(1040,859)), summary y858-903. **PITFALL: Glance 1.1.1 `GlanceModifier.defaultWeight()` takes NO weight arg** — a weighted two-Box progress Row won't compile; draw bars into a bitmap (`drawProgressBarBitmap(context, widthDp, heightDp, fraction, fillColor, trackColor)` → drawRoundRect) and use `ContentScale.FillBounds`, exactly like `drawChartBitmap`.
- **Voice widget Graph-background design (committed `053dbc2`, pushed)**: 4th design `VoiceWidgetDesign.GRAPH_BG`. Whole surface = `drawStatusBackgroundBitmap` (300×120dp): pastel status base `statusColor(remainingFraction)` green `C9E7C5`→amber `FFE3B0`→red `FFC9C6`, chart area filled in `statusDeepColor(fraction)` @35% (deep ramp `4C8B48`→`D99A2B`→`D5534C`), dashed goal line @55%, ring+dot today marker in `2A2A2A`. Foreground: caption/`of daily`/summary `464646`, hero `$remainingPercent%` `2A2A2A` 20sp bold, ADDED caption `1B7A2F`. Background bitmap only used when `isGraphBg && stateSet`, else static preview pill (16dp-corner fix preserved); mid-column `VoiceWidgetChart` skipped. `combineColors(list, t)` = existing multi-stop blend helper. **E2E-verified on API 36** (fresh install, switch in Settings, ~8s re-render): widget `dailyBudget` = `voice-daily-budget-key` = the app's `dailyBudget` (`160.97` = ₹5000/32-day period distributed; note `spentFromDailyBudget` = 0 so remainingFraction = 1.0 → pure green). Pixel targets: base `201,231,197`, chart fill `158,199,153`, goal-line dash at top y=577 `133,180,129` (goalFraction = 1.0 → line at full height), hero "100%" dark, mic `73,93,146`. Percent-left regression: 0 green pixels → static pill restored.
- **Voice widget chart (committed `8c4ead0`, redesign `492d7c8`)**: drawn into a bitmap (Glance 1.1.1 has NO canvas/path composables — `androidx.glance.canvas` absent from the jars; `ContentScale` = `{ Fit, Crop, FillBounds }` only) via `drawChartBitmap(context, designWidthDp=280, chartHeightDp=26, ...)` in `VoiceWidget.kt`, rendered as `Image(provider=ImageProvider(bitmap), contentScale=ContentScale.FillBounds)`. Design palette: top `colorMax` (0xFFDD1414) → bottom `colorMin` (0xFF185ED6), night alphas 0.55/0.38/0.20, light 0.35/0.20/0.08; dashed goal line via `DashPathEffect(6dp,4dp)`; today marker = 8dp halo @0.2 + 4.5dp ring stroke 2dp + 3dp white dot on `fractions.last()`; **range markers** (since `492d7c8`) = colored ring 3.5dp stroke 1.5dp + 2dp white dot on the min/max days of `series.dropLast(1)` (past only; max = topColor, min = bottomColor; drawn before the today marker). Layout positions are shifted down ~46px by the added sub-line + progress bar since `75e12c2` (see the layout entry above for current y-ranges; pre-`75e12c2`: goal line ≈ y744, today marker ≈ (1040,812), red max ≈(398,745)-(411,769), blue min ≈(713,796)-(730,817), marker center = baseline).
- **Voice widget feedback E2E (API 36)**: mic button circle ≈ x[1048..1177] y[670..800] (center ≈ (1112,735)). Tap it → capture +1s: LISTENING ring (primary 25% over surface ≈ `167,176,208`) + inner primary circle + white `ic_equalizer` glyph, caption changes. On the emulator the recognizer is unavailable (`Connection to speech recognition service lost, no #startListening`) → the failure path resets the widget to IDLE and posts the failure notification; `logcat` shows FGS start (`ForegroundServiceTypeLoggerModule` for UID 10217). ADDED (green) / PROCESSING visuals need a real speech service — verify via unit tests (`VoiceWidgetCommitTest`). **Add-another behavior (committed `e8162a2`)**: the ADDED green check is now clickable (`actionRunCallback<VoiceWidgetMicCallback>()` → starts a fresh voice input) and `scheduleFeedbackReset(context)` auto-resets ADDED → IDLE ~4s after a successful commit (only while still ADDED). On-device ADDED E2E still needs a real speech service; user should confirm: wait ~4s after saying a spend → green check reverts to mic; tapping the green check immediately restarts listening.
- **Voice widget state keys** live in the Glance `PreferencesGlanceStateDefinition` datastore (NOT budgetDataStore), written by the shared `CommonWidgetReceiver.observeData` block for every widget: `voice-chart-series-key` (comma-joined plain BigDecimal, last 7 days clamped to period start), `voice-daily-budget-key`, `voice-feedback-state-key` (IDLE/LISTENING/PROCESSING/ADDED), `voice-feedback-text-key` (committed amount for the ADDED caption), plus the existing `stateBudgetPreferenceKey`/`todayBudgetPreferenceKey`/`currencyPreferenceKey`.
- **Widget daily-budget math (backlog #17, committed, pushed)**: the widget's "new per-day budget" split now comes from the canonical `SpendsRepository.whatBudgetForDay(excludeCurrentDay = true, applyTodaySpends = true)` — the SAME call the editor pill uses — NOT a private widget copy (the old `CommonWidgetReceiver.whatBudgetForDay` copy used scale 0 `FLOOR` + `countDaysToToday(finishDate) - 1` and could disagree with the canonical scale 2 `HALF_EVEN`). If the widget's rest-per-day ever looks wrong vs the wallet, fix `SpendsRepository.whatBudgetForDay`, not the receiver.
- **Voice widget permission-flow E2E (API 36, 1280×2856)**: the widget is a Row — left column = amount text, right end = 48dp circular mic button. Band `x[370..1205] y[575..900]`; scan for the primary color `73,93,146` → the rightmost solid cluster (row y=738: runs `1016..1091` + `1120..1159` around the white mic icon) → button center ≈ `(1087,738)`. To test the permission fix with BOTH `RECORD_AUDIO` + `POST_NOTIFICATIONS` revoked (`pm revoke pkg android.permission.POST_NOTIFICATIONS`): tap the mic → logcat must show `START ...cmp=com.danilkinkin.buckwheat/.MainActivity (has extras)` then `act=android.content.pm.action.REQUEST_PERMISSIONS ... GrantPermissionsActivity`; `uiautomator dump` shows the RECORD_AUDIO dialog first ("record audio?", "While using the app" bounds `[152,1351][1128,1519]`), grant → the POST_NOTIFICATIONS dialog follows ("Allow Buckwheat to send you notifications?", "Allow" `[152,1477][1128,1645]`). With both granted, tapping the mic must log `Background started FGS: Allowed ... allowWiu:52` and the result notification appears as `id=301 channel=voice_widget importance=3` in `dumpsys notification --noredact`. `pm grant`/`pm revoke` persist across `adb install -r` (RECORD_AUDIO does NOT — re-grant or re-revoke after reinstall).

## Git Workflow
```powershell
# Our fixes are on the 'our-fixes' branch (origin)
git checkout our-fixes    # to access saved work

# Fresh start on master
git checkout master
git pull upstream master
git push origin master
```

## App Versions
| Library | Version |
|---------|---------|
| Kotlin | 2.2.0 |
| AGP | 8.7.3 (downgraded from 8.11.1 for Android Studio Narwhal 2024.2.1) |
| Hilt | 2.57 |
| Room | 2.7.2 |
| Compose BOM | 1.8.3 |
| Material3 | 1.3.2 |
| DataStore | 1.1.7 |
| Min SDK | 29 |
| Target SDK | 36 |
| Compile SDK | 36 |
| App Version | 4.8.0 (versionCode 29) |

## Common Imports
```kotlin
import androidx.lifecycle.asFlow            // LiveData → Flow
import androidx.lifecycle.asLiveData         // Flow → LiveData
import androidx.lifecycle.livedata            // build LiveData from Flow
import kotlinx.coroutines.flow.first         // Flow.first() (suspend)
import kotlinx.coroutines.flow.map           // Flow.map
import kotlinx.coroutines.launch             // coroutineScope.launch
import androidx.compose.runtime.livedata.observeAsState  // Compose observation
```

## Key DataStore Keys (budgetDataStore)
- `budget` — current budget (String)
- `spent` — total spent (String)
- `dailyBudget` — daily budget (String)
- `spentFromDailyBudget` — spent from daily budget (String)
- `startPeriodDate` — period start (Long ms)
- `finishPeriodDate` — period finish (Long ms)
- `finishPeriodActualDate` — actual finish (Long ms, set when budget finished early)
- `lastChangeDailyBudgetDate` — last daily budget update (Long ms; also written by ASK one-shot handling)
- `lastRecurringAppliedDate` — last day recurring payments were applied (Long ms; backfill marker)
- `currency` — currency code (String)
- `restedBudgetDistributionMethod` — overspend handling (String)
- `hideOverspendingWarn` — overspend warning flag (Boolean)
- `overspendNotifiedStoreKey` — last overspend-crossing flag (Boolean; true while over; resynced to `spentFromDailyBudget > dailyBudget` inside every `edit {}` that mutates today's counter/budget — `addSpent`, `removeSpent` today-branch, `updateDailyBudget`, `setDailyBudget`; never cross it with `edit {}` blocks)
- `knownTags` — pipe-separated known tags (String)

## Key DataStore Keys (settingsDataStore)
- `theme` — ThemeMode name (String)
- `locale` — locale code (String)
- `TUTOR_*` — tutorial stage booleans
- `autoBackupInterval` — backup interval (Int)
- `debug` — debug-mode flag (String; toggled via keyboard "0"×8 + "." + apply)
- `voiceAiApiKeyStoreKey` — Voice AI / category AI API key (String; **deliberately excluded from backup exports** — `asBackupMap()` skips it, so restore never restores or clobbers the key; also excluded from Android cloud backup/transfer via `res/xml/backup_rules.xml` + `data_extraction_rules.xml` — `file: datastore/settings.preferences_pb`. The API key is the ONLY credential in the app; it is never logged).
- `voiceAiProviderUrlStoreKey` — OpenAI-compatible base URL (String; blank → no backend configured → AI falls back offline). Base is normalized by `aiProviderBaseUrl` (base → `/v1/...` suffixes) in `AiBackend.kt`.
- `voiceAiModelStoreKey` — model name (String; **optional since `6e9a23a`** — blank is valid and means "provider default". No defaults/normalization since the single-backend migration `7e0d0f8`; the settings sheet no longer edits it (`10aa493`), so it stays blank unless set by an older build).
- `reminderEnabledStoreKey` — daily budget reminder toggle (Boolean)
- `reminderHourStoreKey` / `reminderMinuteStoreKey` — reminder time (Int); the daily reminder is a one-shot `setWindow` (10-min window) that `DailyBudgetReminderReceiver.rearmNextDay` re-arms at the stored time
- `recurringAlertEnabledStoreKey` / `recurringAlertHourStoreKey` / `recurringAlertMinuteStoreKey` — recurring-payment due-alert toggle + time (Int), defaults 09:00 (`RECURRING_ALERT_DEFAULT_HOUR/MINUTE`); one-shot `setWindow` + `rearmNextDay`
- `spendDigestEnabledStoreKey` (Boolean) / `spendDigestFrequencyStoreKey` (String `WEEKLY`/`MONTHLY`) / `spendDigestHourStoreKey` / `spendDigestMinuteStoreKey` (Int, default 20:00 `SPEND_DIGEST_DEFAULT_HOUR/MINUTE`) — weekly/monthly spend digest; one-shot `setWindow` re-armed +7 days / +1 calendar month by `SpendDigestReceiver.rearm`
- `goalMilestonesNotifiedStoreKey` — per-goal already-notified milestone buckets, serialized `"goalId:bucket;goalId:bucket"` (String; write via `getGoalNotifiedMilestones()`/`setGoalNotifiedMilestones()`)
- `overspendNotifyEnabledStoreKey` — instant overspend notification opt-in (Boolean)
- `aiIntelligenceEnabledStoreKey` — AI Intelligence master toggle (Boolean; default **on** via `aiIntelligenceEnabled(prefs)` in `SettingsRepository.kt`; `false` disables both voice-AI parsing → `NotConfigured` and AI categorization → empty map, offline fallbacks stand in)
- `categoryCapsStoreKey` — per-category spending caps, serialized `"name:amount;name:amount"` (String; drops ≤0 amounts; whole settings DataStore file excluded from cloud backup so caps don't survive device transfer)
- `categoryCapNotifiedStoreKey` — per-category last-announced bucket, serialized `"name:bucket;name:bucket"` (String; bucket 0=none, 1=near/80%, 2=reached/100%; parse via `parseCategoryCapNotified`, which tolerates legacy `"name:bucket@windowStartEpochDay"` entries via `substringBefore('@')`; cleared fully on new budget period and whenever caps are edited). NOTE: interleaved schedules were removed 2026-08-13 — `categorySchedulesStoreKey` no longer exists; cap alerts always measure the budget-period total.

## Glance 1.1.1 per-instance widget facts (verified 2026-08-10 via AAR/sources inspection)
- Widget Glance state (`PreferencesGlanceStateDefinition`) is keyed purely by `appWidgetId` (`createUniqueRemoteUiName(glanceId.appWidgetId)`), so per-instance values are already isolated — `getAppWidgetState(context, definition, glanceId)` / `updateAppWidgetState(...)` read/write only that widget.
- `androidx.glance.appwidget.AppWidgetId(widgetId: Int)` has a **public constructor** and `getAppWidgetId(): Int` — you can build the GlanceId for an app-widget id yourself.
- `GlanceAppWidgetManager.getGlanceIdBy(appWidgetId)` does **NOT** exist in 1.1.1 (checked the classes.jar); to map an `onUpdate` `appWidgetIds` entry to a GlanceId, scan `getGlanceIds(...)` and match `(it as? AppWidgetId)?.getAppWidgetId() == appWidgetId`.
- The 3-arg `updateAppWidgetState(context, glanceId) { it: MutablePreferences -> ... }` overload passes the prefs as a **lambda parameter**, not receiver (the `this[...]` form is the 4-arg `updateAppWidgetState(context, definition, glanceId) { prefs -> prefs.toMutablePreferences().apply { this[...] = ... } }` used by `CommonWidgetReceiver.observeData`).
- `android:configure` on the receiver launches the configure activity at placement; the activity must `setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, id))` (back = RESULT_CANCELED = not placed). Not exercisable via Robolectric — on-device placement is the authoritative check.
- `androidx.glance.unit.ColorProvider` is an **interface** (no public constructor); the public API is the top-level `fun ColorProvider(color: Color): ColorProvider`. Reflection (`getDeclaredConstructor`) always throws. `widget/CommonWidgetReceiver.kt` `Color.toColorProvider()` = `ColorProvider(color = this)` (plain top-level function, no `@Composable`). The old reflection+`LocalContentColor.current` fallback failed with `Error("No set")` (not catchable as `Exception`) → "Can't show content" (LESSONS Issue 35; committed `620d4d5`).
- Release signing: `keystore.properties` (root, gitignored) feeds `app/build.gradle.kts` `signingConfigs.create("release")` (`storeFile`/`storePassword`/`keyAlias`/`keyPassword`); missing file → debug-key fallback. Keystore `keystore/buckwheat-release.jks` (alias `buckwheat`), password backup `C:\Users\suraj\AppData\Local\Temp\opencode\buckwheat-keystore-pass.txt` (KEEP SAFE). Release assemble on this machine: stale `app/build/tmp/kotlin-classes/release` from an aborted run causes "Failed to create MD5 hash ... as it does not exist" → `:app:clean` first (committed `4d0faf7`).

