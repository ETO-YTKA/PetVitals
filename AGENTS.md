# PetVitals - AGENTS.md

Agent instructions for working with this repository.

## Project Overview

Native Android app for pet health tracking. Jetpack Compose (Material 3), Dagger Hilt, Firebase (Auth + Firestore), Navigation Compose with type-safe routes.

## Architecture

Clean architecture with three layers:

```
com.example.petvitals/
├── data/          # Repositories impl, services, Firebase DI
│   ├── di/
│   ├── repository/
│   └── service/
├── domain/        # Repository interfaces, models, business logic
│   ├── models/
│   └── repository/
└── ui/            # Screens, ViewModels, components, navigation, theme
    ├── components/  # Reusable Compose UI elements
    ├── navigation/  # AppRoutes, MainScreen, PetVitalsApp
    ├── screens/     # Feature directories (Screen + ViewModel pairs)
    ├── theme/
    └── utils/
```

Key rules:
- **Repository interfaces** live in `domain/repository/`, **implementations** in `data/repository/`
- **Domain models** live in `domain/models/`
- **ViewModels** are co-located with their Screen in feature subdirectories

## Package Naming

Kotlin convention: **all lowercase, no underscores**.
- ✅ `managefood`, `petprofile`, `passwordreset`
- ❌ `add_edit_food`, `pet_profile`, `password_reset`

## Build / Test Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew clean                  # Clean build outputs
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests
```

## Conventions

- **Hilt injection**: Classes with `@Inject constructor()` need no module. `@Binds` modules are needed for interface-to-impl mappings (e.g., `RepositoryModule`, `ServiceModule`)
- **Navigation**: Uses kotlinx.serialization for type-safe routes (`AppRoutes.kt`)
- **Image handling**: Base64-encoded WebP stored in Firestore; use `ImageProcessor.kt` for encode/decode
- **Date formatting**: Use `DateFormatter.kt` in `ui/utils/` (not java.text.SimpleDateFormat directly in screens)
