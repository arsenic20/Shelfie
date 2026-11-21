# AGENTS.md

Instructions for AI coding agents working on Shelfie. Read this before changing code.

## Project

Shelfie is an Android-only product catalogue MVP (Kotlin, Jetpack Compose, Material 3). Products come from the Fake Store API and are fetched live; Home top-rated, categories and search use a Room cache. See `README.md` and `DECISIONS.md` for the why.

## Architecture rules

- Clean Architecture + MVVM in one module: `ui -> domain <- data`. Package root: `com.example.shelfie`.
- `domain/` is plain Kotlin: models, repository interfaces, validation. No Android, Retrofit or Room imports.
- Room caches Home only (top rated, categories, search). Category products and product details are fetched live from the API and are never cached. `refreshProducts()` writes in one transaction and a failed refresh must never clear the cache.
- ViewModels depend on repository interfaces only, never on DAOs or Retrofit.
- Repository and use-case functions are `suspend` and return `AppResult<T>`. Errors map to `AppError` (`NoNetwork`, `Http`, `Unexpected`) via `safeApiCall`. Always rethrow `CancellationException`.
- ViewModels expose `StateFlow<UiState<T>>` (or a screen-specific sealed state). The UI never calls repositories.
- Dispatchers are injected with `@IoDispatcher`. Never hard-code `Dispatchers.IO`.
- The data source, theme and email provider must stay swappable. A new source is a new repository implementation or remote API, not a UI change. The enquiry is sent through `EnquiryRepository` (currently `FakeEnquiryRepositoryImpl`).

## Conventions

- Dependencies live in `gradle/libs.versions.toml`. Use KSP for Hilt and Room. Do not add the Room Gradle plugin.
- Navigation uses type-safe `@Serializable` routes in `ui/navigation/Route.kt`. ViewModels read arguments with `SavedStateHandle.toRoute()` or the route's key.
- Reuse `ui/components` (`ProductCard`, `SearchBar`, `BackButton`, `ErrorView`). Do not copy-paste error or back-button UI.
- Search runs only when the user taps the search icon or the keyboard search action, never on every keystroke. Trim input, ignore empty input, and escape `%` and `_`.
- Accessibility: every icon needs a content description, touch targets are at least 48dp, and fields have labels.
- Comment only code that needs it. Keep changes small and focused.

## Build and test

```bash
./gradlew :app:assembleDebug        # build
./gradlew :app:installDebug         # install on the running emulator/device
./gradlew :app:testDebugUnitTest    # unit tests; all must pass
./gradlew :app:lintDebug            # lint
```

- Tests for DAOs, the repository and ViewModels use Robolectric with `@Config(sdk = [35])`.
- Flow-collecting ViewModel tests need `Dispatchers.setMain(UnconfinedTestDispatcher())`.
- Use hand-written fakes and `MockWebServer`; the project does not use MockK.
- Add or update tests with every behaviour change.

## Environment notes

- JDK 17, compileSdk 37, targetSdk 36, minSdk 24, Kotlin 2.4.x.
- If the emulator shows an endless loader, its DNS is likely broken. Toggle `adb shell svc wifi disable` then `enable`.

## Git

- Do not commit, push or create branches unless the user asks.
- Never commit secrets.
- Update `DECISIONS.md` when a significant decision changes.
