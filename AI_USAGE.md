# AI Usage

Log of how AI (GitHub Copilot) was used on this project, with the key prompts.
Add an entry for each significant prompt: what was asked, and what came out of it.

## Template

### P-XXX: Title
- **Date:**
- **Prompt:**
- **Outcome:**
- **Human review / changes:**

---

### P-001: Project brief and ground rules
- **Date:** 2026-11-20
- **Prompt:** Shared the product catalogue brief (home top-rated, category page, search, product page, enquiry) and asked the AI not to write code until told to.
- **Outcome:** AI summarised requirements and proposed a swappable architecture (data source, look and feel, email provider).
- **Human review / changes:** Scope narrowed to Android only (no backend experience).

### P-002: Flow and architecture
- **Date:** 2026-11-20
- **Prompt:** Explained the flow: Fake Store APIs (products, categories, category/{name}, products/{id}); store products in Room for search; category drawer; enquiry form with a thank-you toast/dialog only. Asked for an architecture and root folder structure, without code.
- **Outcome:** Clean Architecture + MVVM proposal with Room as single source of truth and a package layout.
- **Human review / changes:** Structure to be reviewed package by package before implementation.

### P-003: Create decision and AI usage docs
- **Date:** 2026-11-20
- **Prompt:** Create a decisions MD file and an AI usage MD file to record decisions and key prompts.
- **Outcome:** `DECISIONS.md` and `AI_USAGE.md` created at the project root.
- **Human review / changes:** None yet.
