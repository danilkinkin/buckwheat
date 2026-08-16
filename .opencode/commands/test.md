---
description: Run the debug unit test suite
agent: build
---

Run the unit test suite and report results.

1. Run `.\gradlew.bat testDebugUnitTest` (Windows) or `./gradlew test` on CI.
2. Show which tests passed/failed and their counts.
3. If any test failed, read the failing test source and fix the root cause.
4. Re-run the affected tests until green, then run the full suite once more.
5. Never delete a failing test to make the build pass.
