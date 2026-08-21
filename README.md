# Hologen

Hologen is a minimal native Android app built with Kotlin and Jetpack Compose.

## Overview
- Calm, minimal UI
- System light/dark theme support
- Single-module Android app structure
- No Firebase, server, NDK, or hardcoded secret keys
- GitHub Actions workflow for Android debug builds on ubuntu-latest

## Project structure
- `app/` — Android application module
- `build.gradle.kts` — root Gradle setup
- `settings.gradle.kts` — project settings
- `.github/workflows/android.yml` — CI build workflow

## Build
This project is set up for Android CI on GitHub Actions. The workflow runs:

```bash
./gradlew assembleDebug
```

To generate the wrapper locally if needed:

```bash
gradle wrapper --gradle-version 8.9
```

## Notes
The app is intentionally minimal and crash-safe, with no placeholder actions or unimplemented UI paths.
