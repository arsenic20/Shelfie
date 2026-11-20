# Decisions Log

Records the key decisions made while building Shelfie. Each entry: context, decision, consequences.
Add new entries at the bottom; never rewrite history, supersede instead.

## Template

### D-XXX: Title
- **Date:**
- **Status:** Proposed | Accepted | Superseded by D-YYY
- **Context:**
- **Decision:**
- **Consequences:**

---

### D-001: Android-only, native Kotlin with Jetpack Compose
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** The team has no backend experience; the MVP is a product catalogue.
- **Decision:** Build an Android app only (Kotlin, Jetpack Compose, Material 3). No custom backend.
- **Consequences:** Faster PoC. Email/enquiry delivery is deferred behind an interface.

### D-002: Clean Architecture + MVVM, single `app` module
- **Date:** 2026-11-20
- **Status:** Accepted (module split to be confirmed)
- **Context:** The PoC must be a foundation that can grow, not a throwaway.
- **Decision:** Layers `presentation -> domain <- data`, organised as packages inside one module.
- **Consequences:** Simple build now; packages can be extracted into modules later.

### D-003: Room is the single source of truth
- **Date:** 2026-11-20
- **Status:** Accepted
- **Context:** Fake Store API has no search endpoint.
- **Decision:** Fetch `/products` and `/products/categories`, store in Room; UI reads only from Room. Search, top-rated and category queries run in Room.
- **Consequences:** Offline-friendly and searchable. Data source can be replaced by changing only the repository/remote datasource.

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
- **Decision:** `SubmitEnquiryUseCase` -> `EnquiryRepository` interface with a no-op/fake implementation. UI shows a "Thank you" toast/dialog after form validation.
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
- **Status:** Proposed
- **Decision:** Hilt, Retrofit + Moshi/kotlinx.serialization, Room, Coroutines/Flow, Navigation Compose, Coil, JUnit/MockK/Turbine.

## Open questions
- Single module vs multi-module?
- "Top rated": `rating.rate` only, or weighted with `rating.count`?
- Search scope: title + description, or also category?
- Enquiry form fields (name, email, phone optional, message)?
- Min SDK and use of a version catalog (`libs.versions.toml`)?
