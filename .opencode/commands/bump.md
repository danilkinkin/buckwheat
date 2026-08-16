---
description: Bump the app version (versionCode and/or versionName)
agent: build
---

Bump the app version in `app/build.gradle.kts`.

Current version:
- `versionCode = 30`
- `versionName = "4.9.0"`

`$ARGUMENTS` may specify the new version. Examples:
- `/bump patch` — bump `versionName` patch segment (4.9.0 -> 4.9.1) and increment `versionCode` by 1.
- `/bump minor` — bump the minor segment (4.9.0 -> 4.10.0) and increment `versionCode` by 1.
- `/bump 5.0.0` — set `versionName` to exactly `5.0.0` and increment `versionCode` by 1.

Always increment `versionCode` by exactly 1 regardless of how much `versionName` changes, unless an explicit versionCode is given.

Steps:
1. Read `app/build.gradle.kts` to confirm current values.
2. Apply the change.
3. Re-read `app/build.gradle.kts` to verify the change.
4. Report old -> new values.
