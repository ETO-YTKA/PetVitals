# PetVitals Agent Guide

## Snapshot

- Single-module native Android app: root `PetVitals`, module `:app`.
- Package/namespace/application id: `com.example.petvitals`.
- Entry point: `MainActivity` renders `ui.navigation.PetVitalsApp()`.
- Hilt app class: `PetVitalsHiltApp`; Timber is planted only for debug builds.
- Firebase config: `app/google-services.json`; ignored by `.gitignore`. Do not read, print, edit, or stage it unless explicitly requested.
- For Android/Kotlin architecture, implementation, debugging, refactoring, or review, use the `android-kotlin-development` skill when available.
- Repo-specific facts in this file override generic Android guidance from that skill.

## Commands

- Use the checked-in Gradle wrapper from repo root: `.\gradlew.bat`.
- Build debug APK: `.\gradlew.bat :app:assembleDebug`.
- Unit tests: `.\gradlew.bat :app:testDebugUnitTest` or `.\gradlew.bat test`.
- One test: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.petvitals.ExampleUnitTest.addition_isCorrect"`.
- Lint: `.\gradlew.bat :app:lintDebug`.
- Device/emulator tests: `.\gradlew.bat :app:connectedDebugAndroidTest`.
- No CI, ktlint, detekt, formatter, or pre-commit config was found; Gradle tasks are the source of truth.

## Architecture

- Treat existing code as legacy; improve toward Clean Architecture only where touched.
- Keep `domain/` Android/Firebase-free: pure models, repository interfaces, validators, and use cases.
- Keep Firebase/Auth details, DTO mapping, and repository implementations in `data/`.
- Bind repository interfaces in `di/RepositoryModule.kt`; bind service interfaces in `di/ServiceModule.kt`.
- Provide Firebase SDK types only from `di/FirebaseModule.kt`; do not inject them into UI or domain code.
- UI uses Jetpack Compose Material 3 under `ui/`; feature packages are lowercase, e.g. `managefood`, `petprofile`, `passwordreset`.

## Navigation

- Typed routes live in `ui/navigation/AppRoutes.kt` using kotlinx serialization.
- Auth/splash routing is in `PetVitalsApp.kt`.
- Logged-in graph and bottom bar are in `MainScreen.kt`.
- New destinations need an `@Serializable` route plus a matching `composable<T>` in the correct graph.

## Implementation

- Use Hilt constructor injection; add Dagger bindings only for interface-to-implementation mappings.
- ViewModels use `@HiltViewModel`, `@Inject`, private `MutableStateFlow`, public `asStateFlow()`, and `viewModelScope`.
- Keep ViewModels thin: no Firebase queries, DTO mapping, or business rules.
- Add domain use cases for shared, multi-step, permission-sensitive, or hard-to-test logic.
- Model UI state immutably and one-off events explicitly.
- Reuse `ui/components/`, `ui/theme/Dimen.kt`, `ui/utils/ImageProcessor.kt`, and `ui/utils/DateFormatter.kt`.
- Split large Compose screens into private stateless composables in the same file before creating new files.
- Prefer focused unit tests for domain validators, use cases, and mapping logic.
- Keep Kotlin package names lowercase with no underscores.
