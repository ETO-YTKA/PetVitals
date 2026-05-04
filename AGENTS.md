# PetVitals Agent Guide

Compact guidance for future OpenCode sessions. Keep this file limited to repo-specific facts an agent would likely guess wrong.

## Project Shape

- Single-module native Android app: root project `PetVitals`, only included module is `:app`.
- Package/namespace/application id is `com.example.petvitals`; main entrypoint is `MainActivity`, which renders `ui.navigation.PetVitalsApp()`.
- `PetVitalsHiltApp` is the `@HiltAndroidApp` application class and plants Timber only for debug builds.
- The Firebase config file is `app/google-services.json`; it is ignored by `.gitignore`, so do not print, edit, or stage it unless explicitly requested.

## Android/Kotlin Guidance

- For Android/Kotlin architecture, implementation, debugging, refactoring, or review, use the `android-kotlin-development` skill when available.
- Repo-specific facts in this file override generic Android guidance from the skill.

## Commands

- Prefer the checked-in Gradle wrapper. On Windows use `./gradlew.bat`; on Unix shells use `./gradlew`.
- Build debug APK: `./gradlew.bat :app:assembleDebug`.
- Run local unit tests: `./gradlew.bat :app:testDebugUnitTest` or all variants with `./gradlew.bat test`.
- Run one local unit test class/method: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.petvitals.ExampleUnitTest.addition_isCorrect"`.
- Run Android lint: `./gradlew.bat :app:lintDebug`.
- Run instrumented tests on a connected device/emulator: `./gradlew.bat :app:connectedDebugAndroidTest`.
- No CI workflows, ktlint, detekt, formatter config, or pre-commit config were found; Gradle tasks are the executable source of truth.

## Architecture

- Treat this as legacy code that should move toward Clean Architecture when touched. Do not expand old shortcuts just because they already exist.
- `domain/` should stay Android/Firebase-free: put pure models, repository interfaces, validation, and non-trivial use cases there.
- `data/` owns Firebase/Auth details, DTO mapping, and repository implementations. Bind repository interfaces to implementations in root-level `di/RepositoryModule.kt`.
- `data/service/` contains Firebase/Auth services; interface-to-implementation service bindings are in root-level `di/ServiceModule.kt`.
- Root-level `di/FirebaseModule.kt` provides singleton `FirebaseFirestore` and `FirebaseAuth` instances. Do not inject Firebase SDK types into UI or domain code.
- UI is Jetpack Compose Material 3 under `ui/`; feature screens keep `Screen` and `ViewModel` files together in lowercase package directories such as `managefood`, `petprofile`, and `passwordreset`.

## Navigation

- Navigation uses kotlinx-serialization typed routes in `ui/navigation/AppRoutes.kt`.
- Top-level auth/splash routing is in `PetVitalsApp.kt`; the logged-in nested graph and bottom bar are in `MainScreen.kt`.
- When adding a destination, add the `@Serializable` route in `AppRoutes.kt` and wire the matching `composable<T>` in the correct graph.

## Implementation Conventions

- Use Hilt constructor injection. Only add a Dagger module binding when mapping an interface to an implementation.
- ViewModels use `@HiltViewModel`, `@Inject constructor`, `MutableStateFlow`/`asStateFlow`, and `viewModelScope` or the shared `PetVitalsAppViewModel.launchCatching` pattern.
- Keep ViewModels thin: they coordinate UI state and call use cases/repositories, but should not contain Firebase queries, mapping logic, or business rules.
- Add a use case in `domain/` when logic is shared, multi-step, permission-sensitive, or hard to test inside a ViewModel.
- Expose immutable UI state from ViewModels; keep mutable state private and model one-off events explicitly instead of hiding them in nullable state when possible.
- Reuse shared Compose components from `ui/components/` and spacing tokens from `ui/theme/Dimen.kt` before creating new UI primitives.
- Split large Compose screens into private stateless composables in the same file first; only create new files when a component is reusable or the file becomes hard to scan.
- Use `ui/utils/ImageProcessor.kt` for pet image Base64/WebP encode/decode; avatars are stored as strings in Firestore models.
- Use `ui/utils/DateFormatter.kt` for screen-level date formatting instead of introducing ad hoc formatter calls.
- Prefer focused unit tests for domain validators/use cases and repository mapping logic before UI-heavy tests.
- Keep Kotlin package names lowercase with no underscores, matching existing feature directories.
