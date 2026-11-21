# Shelfie

An Android product catalogue MVP (Kotlin, Jetpack Compose, Material 3). Customers browse top-rated products, explore categories from a drawer, search, view product details and submit an enquiry.

Product data comes from the [Fake Store API](https://fakestoreapi.com) and is fetched live, with an extra Room cache for the Home screen only. It is built as a foundation to grow, not a throwaway prototype: the data source, look and feel and email provider are each isolated behind a single seam.

## Why Android, and why no backend

The team has no backend experience, so the MVP is a native Android app that talks directly to the public Fake Store API. Everything a backend would do for this MVP is handled on the device:

- **Search:** the API has no search endpoint, so the products fetched for Home are stored in Room and searched there with SQL `LIKE`.
- **Caching (extra, Home only):** only the Home top-rated list and the category list (drawer) are cached in Room, so Home works offline. Category products (`GET /products/category/{name}`) and product details (`GET /products/{id}`) are always fetched live and never cached.
- **Enquiry:** delivery to the sales inbox is stubbed behind an interface (see `DECISIONS.md`).

## Features

| Screen | Behaviour |
|---|---|
| Home | "Welcome to Shelfie", search bar, the 5 top-rated products in a grid, a drawer listing categories |
| Category | Products in the category sorted by rating: "Top rated" (3) then "More in this category" |
| Search | Runs on the search icon or the keyboard's search action (never per keystroke). Input is trimmed, empty input is ignored, `%` and `_` are escaped. The term stays editable |
| Product details | Large image, title, price, rating, category, description, Enquire button |
| Enquiry | Name, email, phone (optional), message with inline validation. Success shows a "Thank you" dialog and returns to the product |

## Architecture

Clean Architecture with MVVM, in a single Gradle module. Dependencies point inward: `ui -> domain <- data`.

```
┌──────────────────────────── ui (Compose + ViewModels) ───────────────────────────┐
│  Home   Category   Search   Detail   Enquiry      navigation/  components/  theme/ │
│  each screen: Screen composable  <--  StateFlow<UiState>  <--  @HiltViewModel     │
└───────────────────────────────────────┬───────────────────────────────────────────┘
                                        │ depends on interfaces only
┌───────────────────────────── domain (pure Kotlin) ───────────────────────────────┐
│  model: Product, Rating, Enquiry                                                  │
│  repository: ProductRepository, EnquiryRepository   (interfaces)                 │
│  validation: EnquiryValidator                                                     │
└───────────────────────────────────────┬───────────────────────────────────────────┘
                                        │ implemented by
┌─────────────────────────────── data ─────────────────────────────────────────────┐
│  ProductRepositoryImpl ──► Room (ProductDao, CategoryDao)   <- Home cache only    │
│        │                                      (top rated, categories, search)      │
│        └──► FakeStoreApi (Retrofit + kotlinx.serialization) -> refresh into Room  │
│  FakeEnquiryRepositoryImpl  (stub; replace with a real email provider)            │
│  mapper/: Dto <-> Entity <-> domain model                                         │
└───────────────────────────────────────────────────────────────────────────────────┘
        core/: AppResult, AppError, UiState, safeApiCall, @IoDispatcher
        di/:   Hilt modules (Network, Database, Repository, Dispatcher)
```

Data flow: Home, the drawer categories and search read from Room through `Flow`. `refreshProducts()` fetches from the API and upserts into Room in one transaction; a failed refresh leaves the cache untouched, so Home keeps working offline. The Category and Product detail screens call the API directly (no cache) and show an error with Retry when offline. With no network and an empty cache, Home shows an error with Retry.

Package layout (`app/src/main/java/com/example/shelfie`):

```
core/      common result/error types, network helper
data/      local (Room), remote (Retrofit), mapper, repository impls
di/        Hilt modules
domain/    model, repository interfaces, validation
ui/        home, category, search, detail, enquiry, components, navigation, theme
```

## Tech stack

Kotlin 2.4, Jetpack Compose + Material 3, Hilt (KSP), Retrofit + OkHttp + kotlinx.serialization, Room (KSP), Coil, Navigation Compose (type-safe routes), Coroutines/Flow. Dependencies are managed in `gradle/libs.versions.toml`.

## Build and run

**Branch:** the code is on `feature/Shelfie-001/browse_setup` (open PR into `main`). `main` only holds the initial template, so clone the feature branch:

```bash
git clone -b feature/Shelfie-001/browse_setup https://github.com/arsenic20/Shelfie.git
```

Requirements: Android Studio with JDK 17, **Android SDK platform 37** (install it in Android Studio: Settings > Android SDK; the build fails on SDK 36.x), an emulator or device on Android 7.0+ (minSdk 24) with internet access.

```bash
# Build and install on a running emulator/device
./gradlew :app:installDebug

# Launch
adb shell am start -n com.example.shelfie/.MainActivity
```

Or open the project in Android Studio and press Run. The API base URL is `BuildConfig.BASE_URL` (set in `app/build.gradle.kts`).

If the emulator shows a never-ending loader, its network is probably down. Toggle it with `adb shell svc wifi disable && adb shell svc wifi enable`.

## Tests

```bash
./gradlew :app:testDebugUnitTest   # all unit tests (JVM, Robolectric)
./gradlew :app:lintDebug           # lint
```

Coverage: DTO/entity/domain mapper, DAO queries on an in-memory Room database (search, top-rated, category ordering, wildcard escaping, case-insensitive and partial matches), repository with `MockWebServer` (refresh, offline, failure keeps cache, live category/detail calls that are not cached), the enquiry validator, and the Search, Category and Enquiry ViewModels.

## Docs

- `DECISIONS.md`: assumptions, trade-offs and what comes next
- `AI_USAGE.md`: how AI tooling was used
