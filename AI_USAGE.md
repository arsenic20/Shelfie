# AI Usage

How I used AI on Shelfie, what guided it, and the prompts that made the biggest difference.

## Tools and models

| Tool | Used for |
|---|---|
| GitHub Copilot app / CLI agent (Claude Sonnet 5.5) | Planning the architecture, writing code, tests and docs, running builds and checking on the emulator |

I stayed in control of scope: I asked for one feature at a time and reviewed the result on the emulator before moving on. I told the agent not to write code until I had approved the plan.

## Agent instruction files

- **`AGENTS.md`** (repo root): architecture rules (Room caches Home only; category and detail are live, interfaces in domain, `AppResult`/`UiState`, injected dispatchers), conventions, build and test commands, accessibility rules and git rules. It keeps the agent consistent across sessions.
- **`DECISIONS.md`**: the decisions I made, so the agent and reviewers know the "why".
- **`README.md`**: architecture diagram and run instructions.

## Plan

1. Shared the brief and asked the AI not to code yet.
2. Decided on Android only with no backend, using the Fake Store API directly and Room for search.
3. Got a Clean Architecture + MVVM proposal and folder structure, then approved it.
4. Built step by step: setup and API, detail page, home fixes, Room and search, categories, enquiry, final review. Each step ended with a build, unit tests and an emulator check.

## Key prompts

### Prompt 1: app flow and architecture (before any code)

> Don't start coding yet. I am explaining the complete flow, then give me an architecture.
>
> - The app is Android only. We have no backend experience, so we use the Fake Store API directly.
> - Products: `GET /products`. Categories: `GET /products/categories`.
> - Products by category: `GET /products/category/{name}`. Product detail: `GET /products/{id}`.
> - The drawer lists the categories using the names returned by the API.
> - Tapping a category opens the products of that category. Tapping a product opens the product detail page.
> - There is no search API. When Home calls the products API, store the response in Room, and search the title/description locally with a text match.
> - The enquiry is a form with no API. On submit just show a "Thank you" message.
>
> At the end: suggest an architecture and show the root folder/package structure with the exact file names, so I can approve each package and file before you write code.

Why it mattered: the flow, the API list and the no-backend limit were all fixed up front. The agent proposed Clean Architecture + MVVM (`ui` / `domain` / `data` / `core` / `di`) and I approved it before any code was written. The folder structure is exactly the one in the repo:

```
com/example/shelfie
├── MainActivity.kt, ShelfieApp.kt
├── core/    common/{AppResult, Dispatchers}, network/SafeApiCall
├── data/    local/{AppDatabase, ProductDao, ProductEntity}
│            remote/api/FakeStoreApi, remote/dto/{ProductDto, ProductDetailResponseDto}
│            mapper/ProductMapper
│            repository/{ProductRepositoryImpl, FakeEnquiryRepositoryImpl}
├── di/      NetworkModule, DatabaseModule, DispatcherModule, RepositoryModule
├── domain/  model/{Product, Enquiry}
│            repository/{ProductRepository, EnquiryRepository}
│            validation/EnquiryValidator
└── ui/      home, category, detail, search, enquiry (Screen + ViewModel each)
             components/{ProductCard, SearchBar, CategoryName, CommonUi}
             navigation/{Route, ShelfieNavGraph}, theme/
```

### Prompts 2 to 6: one feature per step

After the plan was approved I built the app in five steps. Each prompt names the screen, the API call, the data source and the error/empty behaviour, so the agent has nothing to guess.

**Prompt 2: Home screen**

> Build the Home screen first, with a navigation drawer and the title "Welcome to Shelfie". Call the products API `GET /products` and the categories API `GET /products/categories` on the Home screen. Handle errors (no network, HTTP error, unexpected) with a retry option, then show the 5 top-rated products in a grid. Also use Navgraph for navigate to the screens

Why it mattered: it fixed the first screen, both API calls, error handling and the "top 5 by rating" rule in one small, testable step.

**Prompt 3: Room and caching**

> After the products and categories API calls succeed, store the products in Room DB. Use this as a cache for the Home page, so the top-rated products and the category list still show when the network fails. A failed refresh must never clear the cache.

Why it mattered: it gave the app a local copy that search needs and made Home work offline without touching the other screens.

**Prompt 4: Search**

> Add a search bar below the "Welcome to Shelfie" title. When the user taps the search icon, query the Room product data for the entered text (title, description and category). Open the Search screen and show the matching products. If nothing matches, show: No products found for "entered value".

Why it mattered: the API has no search endpoint, so this moves search into Room. It also fixes the trigger (icon tap only), the results screen and the empty-state text.

**Prompt 5: Product details**

> When the user taps a product, call `GET /products/{id}` and open the Product Detail screen. It has a toolbar with a back button. Below it show the image, then the title, price, rating and category, and the description last.

Why it mattered: it defines the live detail call and the exact layout order, so the screen was right the first time.

**Prompt 6: Categories**

> When the user taps a category in the drawer, call `GET /products/category/{name}` and open the Category screen. Show the 3 top-rated products first, and the rest under "More in this category".

Why it mattered: category pages use the live endpoint instead of Room, and the top-rated vs. more split is stated explicitly.

### Polishing after review

I then ran the app on the emulator and asked for small corrections, for example equal card heights, a search icon inside the field, a bigger centred back button, removing category products and details from the cache (cache only for Home), and showing an error instead of an endless loader when there is no network and no cache. The enquiry form (fake repository, `EnquiryValidator`, "Thank you" dialog) and the final review (accessibility, tests, docs) were separate prompts.

## Where the AI got it wrong

Things I found while reviewing the app and the code, and how they were fixed:

- **Caching too much:** it cached category products and product details in Room. I asked for the live endpoints (`GET /products/category/{name}` and `GET /products/{id}`) and removed that caching. Only the Home top-rated list and the category list are cached, and this is written in `README.md` and `DECISIONS.md`.
- **Uneven product cards:** card heights were not fixed, so some cards were bigger and some smaller. Fixed with a fixed image size and a two-line title area so every card has the same height.
- **Search button outside the search box:** it added a separate blue Search button next to the field. I asked for a search icon inside the text field, and the search runs when that icon (or the keyboard search action) is tapped.
- **Endless loader on Home:** with no network and nothing in the cache, Home showed the loader forever. Fixed: Home now shows an error with a Retry button. I also added OkHttp timeouts so a hung request ends.
- **Conflict and delete cases missed on refresh:** it did not handle API data that changed after it was cached. Now refresh upserts rows (changed products overwrite the old ones, no duplicates) and deletes cached products that no longer exist in the API, in one transaction. A failed or empty response never wipes the cache.
- **Text symbols as icons:** it used "←" and "☰" without content descriptions. They were replaced with real icons in the final accessibility pass.
- **Emulator, not the app:** the first Home loader problem came from the emulator's broken DNS. The AI first suspected the app code, and only later reset the emulator's wifi.
- **Detail screen could load forever:** a product missing from both cache and API left the detail screen loading. Found in the final review and fixed; the detail screen now shows an error with Retry.

## Time spent

| Activity | Time    |
|---|---------|
| Planning and architecture | 45min   |
| Implementation with AI | 45min   |
| Review, fixes and testing | 40min   |
| Documentation | 50min   |
| **Total** | 3 hours |
