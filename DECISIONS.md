# Decisions Log

The key decisions I made while building Shelfie, in my own words. Each entry: context, decision, consequences.
Add new entries at the bottom; never rewrite history, supersede instead.

## Template

### D-XXX: Title
- **Date:**
- **Status:** Proposed | Accepted | Superseded by D-YYY
- **Context:**
- **Decision:**
- **Consequences:**

---

### D-001: Use the Fake Store API directly, no backend; Android only
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** I am an Android developer and don't have much experience in backend.
- **Decision:** I decided to call the Fake Store API directly from the app and build only the Android side (Kotlin, Jetpack Compose, Material 3).
- **Consequences:** Faster proof of concept. Anything a backend would do (search, sending the enquiry email) is handled in the app or stubbed behind an interface.

### D-002: Clean Architecture + MVVM, single `app` module
- **Date:** 2026-11-20
- **Status:** Accepted (module split to be confirmed)
- **Context:** The PoC must be a foundation that can grow, not a throwaway.
- **Decision:** Layers `presentation -> domain <- data`, organised as packages inside one module.
- **Consequences:** Simple build now; packages can be extracted into modules later.

### D-003: Use Room DB so I can implement search
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** The Fake Store API docs have no search endpoint.
- **Decision:** I fetch all products once and store them in Room. Search is a Room `LIKE` query on title, description and category, and Home, categories and search read from Room (the Home cache).
- **Consequences:** Search works offline and is fast. A failed refresh never wipes the cache. The data source can change later without touching the UI.

### D-004: Data source behind `ProductRepository` interface
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** Product data source is expected to change.
- **Decision:** Domain depends on an interface; Fake Store implementation lives in `data/`.
- **Consequences:** Swapping the API does not affect domain or UI.

### D-005: Enquiry is a stub in the PoC
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** No backend; email provider is expected to change.
- **Decision:** `EnquiryRepository.submit()` is an interface with `FakeEnquiryRepositoryImpl` (1s delay, always succeeds). The ViewModel calls the repository directly; no separate use case. UI shows a "Thank you" dialog after validation.
- **Consequences:** Real email provider can be added later without UI changes.

### D-006: Themeable UI
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** Look and feel is being designed separately and will change.
- **Decision:** Centralise colours, typography and shapes in `presentation/theme`; build reusable components in `presentation/components`.
- **Consequences:** Applying the final design touches the theme layer mostly.

### D-007: Categories shown in the navigation drawer
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** Categories come from the API response.
- **Decision:** Drawer lists categories dynamically (cached in Room); selecting one opens the category screen.
- **Consequences:** New categories appear without code changes.

### D-008: Libraries
- **Date:** 2026-11-20
- **Status:** Accepted
- **Decision:** Hilt (KSP), Retrofit + kotlinx.serialization, OkHttp, Room (KSP), Coroutines/Flow, Navigation Compose (type-safe routes), Coil, JUnit, MockWebServer and Robolectric for tests (hand-written fakes instead of MockK).
- **Consequences:** `kotlinx.serialization` avoids a second JSON library. Robolectric lets Room and `SavedStateHandle` tests run on the JVM.

### D-009: Toolchain bump
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** The current Retrofit, OkHttp, Coil and Room releases require Kotlin 2.4.x and compileSdk 37.
- **Decision:** Kotlin 2.4.10 and compileSdk 37. targetSdk stays 36, minSdk 24.
- **Consequences:** Needs the Android 37 SDK platform installed. Behaviour is still governed by targetSdk.

### D-010: Refresh never destroys the cache
- **Date:** 2026-11-20
- **Status:** Accepted
- **Decision:** `refreshProducts()` fetches products and categories first, then writes everything in one Room transaction and removes products that disappeared remotely. An error or an empty response leaves the cache as it was.
- **Consequences:** Offline use shows the last good data. The loader or error state appears only when the cache is empty.

### D-011: Search and "top rated" definitions
- **Date:** 2026-11-20
- **Status:** Accepted (assumption, to confirm with the product owner)
- **Decision:** Top rated means `rating.rate` descending, with `rating.count` as tie-breaker. Home shows 5, a category page shows 3 under "Top rated". Search is case-insensitive substring matching on title, description and category, triggered by the search icon only, with `%`/`_` escaped.

### D-012: Category and detail pages are live; caching is Home only
- **Date:** 2026-11-21
- **Status:** Accepted
- **Decision:** Tapping a category calls `GET /products/category/{name}` and the detail page calls `GET /products/{id}`. Neither result is cached. Caching in Room is an extra feature I added for Home only: the top-rated list and the category list. Search reads the products stored by the Home refresh. This replaces my earlier choice of reading category pages from Room.
- **Consequences:** Category and detail data is always fresh and follows the API as specified, but those screens need a network and show an error with Retry offline. With no network and an empty cache, Home shows an error with Retry instead of an endless loader (OkHttp call timeout 20s).

### D-013: Form state in the ViewModel, validation in the domain
- **Date:** 2026-11-21
- **Status:** Accepted
- **Decision:** `EnquiryValidator` is a pure object in `domain/validation`. `EnquiryViewModel` holds form state, errors, submitting and submitted flags, so rotation keeps the form.
- **Consequences:** Rules are unit-testable without Android. Form state is not restored after process death.

## Assumptions

- Products are only read; there is no cart, checkout, accounts or admin.
- The Fake Store API stays reachable and its shape is stable. Product metadata is not important yet.
- English only, portrait phones. Light/dark follows the system and the final design is not applied yet.
- Prices are shown as `$` plus the raw number.
- Enquiry fields: name, email, phone (optional), message.
- Search products: search for the entered text (title, description and category).

## No-backend decision and approval

I don't have much backend experience, so the PoC is Android-only and uses the public Fake Store API directly (D-001, D-003).

## Simplified for the PoC

What I kept simple on purpose, and what changes later.

| Item | In the PoC | Later, if this becomes production |
|---|---|---|
| Enquiry email | `FakeEnquiryRepositoryImpl` waits 1s and succeeds; nothing is sent. The user sees a "Thank you" dialog | Implement `EnquiryRepository` with an email provider or backend endpoint and change one binding in `RepositoryModule` |
| Product source | Fake Store API, called directly from the app (no backend) | Replace the Retrofit API and mapper in `data/`; domain and UI stay unchanged |
| Search | The API has no search, so I query the products cached in Room with `LIKE` (title, description, category) | A server-side search endpoint, or a Room FTS table for a large catalogue |
| Caching | Room caches Home only: top-rated products, the category list and the products used by search. Category products and product details are always live | Decide a proper cache policy per screen (expiry, refresh rules) |
| Sync | Refresh upserts changed products and deletes ones removed from the API, in one transaction. A failed or empty response never wipes the cache | Incremental sync and an "offline, showing saved data" indicator |
| Offline | Home works offline from the cache. Category and detail screens show an error with Retry | Cache them too if the business needs offline browsing |
| Look and feel | Default Material 3 theme, navy basket launcher icon | Apply the designers' tokens in `ui/theme` |
| Icons | Standard Material icons (`material-icons-core`) | The designers' custom icon set |
| Strings | Hard-coded English | Move to `strings.xml` and localise |
| Room schema | Version 1, export off, no migrations | Enable schema export and write migrations |
| Single module | Packages inside `app` | Split into `domain`, `data` and `feature-*` modules |

## How the design grows

Layers are `ui -> domain <- data`, so most changes touch one place only.

- **New data source:** implement `ProductRepository` or swap the remote part of `ProductRepositoryImpl` (API, DTOs, mapper). The UI does not change. Room stays the Home cache.
- **Real enquiries:** add an `EnquiryRepository` implementation and rebind it in `RepositoryModule`. If delivery must survive offline, queue enquiries in Room and send them with WorkManager.
- **Rebrand:** change `ui/theme` and the shared components in `ui/components` (`ProductCard`, `SearchBar`, `CommonUi`).
- **New screens:** add a `Route`, a ViewModel and a screen; nothing else changes.
- **Cache more screens:** the repository decides what is cached, so category or detail caching can be added inside `ProductRepositoryImpl` without UI changes.
- **Larger catalogue:** add paging (Paging 3) and a Room FTS table for search.
- **Agent guidance:** `AGENTS.md` holds the architecture rules, so new work from me or an AI agent follows the same structure.

## What I would do next with another day

- Add Compose UI tests and a navigation test for the full browse, detail and enquire flow.
- Move all strings to resources and add accessibility checks.
- Add loading skeletons, pull-to-refresh and an "offline, showing saved data" banner on Home.
- Show the cached Home data with a message when a refresh fails, instead of silently keeping it.
- Add retry with backoff for the Home refresh.
- Persist the enquiry form across process death, and add a real email provider behind `EnquiryRepository`.
- Set up CI (build, unit tests, lint) and enable Room schema export with a migration test.
- Add a `SubmitEnquiryUseCase` if enquiry rules grow beyond validation.
- Confirm with the product owner how "top rated" is defined and whether the data source will change.

## Open questions

- Single module vs multi-module?
- Should "top rated" be weighted by `rating.count` (current tie-breaker only)?
- Which sales inbox and email provider will be used?
