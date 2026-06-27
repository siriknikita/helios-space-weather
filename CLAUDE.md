# Helios — Claude Code instructions

Android app tracking the NOAA planetary **Kp index** with geomagnetic-storm alerts. Single Gradle
module (`:app`), Kotlin + Jetpack Compose, package `com.helios.spaceweather`. Min/target SDK 26/35,
JDK 17. `README.md` has the full architecture, data-flow diagram, and screenshots — read it for
context; this file covers how to work in the repo.

## Commands

Use the `Justfile` recipes (run `just` to list them) rather than calling Gradle directly:

| Task | Command |
|---|---|
| Bootstrap toolchain (writes `local.properties`) | `just setup` |
| Build debug APK | `just build` |
| Build minified, debug-signed release APK | `just build-release` |
| Install + launch on a connected device | `just run` |
| Unit tests | `just test` |
| Android Lint (debug) | `just lint` |
| Pre-push gate (lint + test + build) | `just check` |
| Publish a GitHub release with the APK | `just release [tag]` |

`just build` / `just release` auto-run `setup.sh` on a fresh clone (when `local.properties` is
missing), so they work straight after `git clone`. Run **`just check`** before pushing.

## Architecture rules

Clean Architecture with an **MVI** UI layer. Dependencies point inward — never violate this:

- **`domain/`** — pure Kotlin (models + `KpRepository` interface). No Android, no framework imports.
- **`data/`** — implements `domain` interfaces. `remote/` (Retrofit/NOAA), `local/` (Room),
  `datastore/` (anti-spam prefs), `repository/` (`KpRepositoryImpl`).
- **`ui/`** — Compose screens + `ViewModel`s. Each screen has a `…Contract` (State/Intent), a
  `ViewModel` exposing `StateFlow`, and a `…Screen` composable. Keep new UI in this shape.
- **`di/`** — Hilt modules wire everything. New cross-layer dependencies get bound here, not
  constructed inline.
- `work/` (WorkManager sync), `notification/` (storm alerts), `core/` (theme, util) are support.

## Conventions & gotchas

- **Offline-first:** the network is never on the read path. The UI renders from the Room cache; the
  sync worker writes to it. Don't add direct network reads to a screen/ViewModel.
- **NOAA format tolerance lives in one place** — `data/remote/dto/NoaaKpDeserializer.kt` parses both
  the modern numeric-object and legacy array-of-arrays shapes, case-insensitively. If the NOAA
  payload shape changes, fix it *there*; the rest of the app only sees normalized rows. Covered by
  `NoaaKpParsingTest` against real payloads — update that test alongside any parser change.
- **Anti-spam alert rule is pure and unit-tested** — `NotificationPreferences.decide(...)`: alert
  only at **Kp ≥ 5** AND (last alert > 12h ago OR the storm escalated). Pinned by
  `AntiSpamDecisionTest`. Keep the decision function pure (no I/O) so it stays testable.
- **Per-app locale (en/uk):** language switches at runtime via `AppCompatDelegate.setApplicationLocales`.
  Notification copy must resolve against the active locale too. New user-facing strings go in both
  `values/` and `values-uk/` (resource set is restricted to `en` + `uk`).
- **Release signing:** the release build is signed with the Android **debug key** (see
  `app/build.gradle.kts`) so `assembleRelease` produces an installable `app-release.apk` with no
  committed secrets. Swap in a real upload keystore there if this ever ships to a store.
- **APKs are gitignored** (`*.apk`) — `fd`/`rg` won't list built APKs; that's expected, not a missing
  build. Never commit APKs, keystores (`*.jks`/`*.keystore`), or `local.properties`.
- Kotlin official code style (`kotlin.code.style=official`). Match the surrounding file's idiom.

## Releasing

`just release` reads `versionName` from `app/build.gradle.kts` and tags `v<versionName>`. To cut a new
version: bump `versionCode` + `versionName` there first, then `just release`. It builds the signed APK
and publishes a GitHub release via the `gh` CLI (must be authenticated).
