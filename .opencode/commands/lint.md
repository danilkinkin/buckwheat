---
description: Run spotless and lint checks
agent: build
---

Run code-quality checks and fix violations.

1. Run `.\gradlew.bat spotlessCheck` first.
2. Run `.\gradlew.bat lintDebug`.
3. For each violation, fix the source file (do not suppress unless the project already suppresses that rule).
4. Re-run both until clean. Report what was fixed.
