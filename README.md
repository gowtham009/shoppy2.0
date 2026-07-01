# shoppy2.0 (GK Shoppy)

This repository contains the server-side rewrite of GK Shoppy. The current branch contains a Spring Boot backend (Thymeleaf templates) that replaces the previous React frontend. The backend includes authentication, cart/order persistence, a payment scaffold (Stripe REST fallback), and a SerpAPI integration.

IMPORTANT: This README focuses on the backend project located in `backend/`.

## Quick start (backend)

Requirements:
- Java 17 (or compatible runtime configured in `pom.xml`)
- Maven

Run locally:

```bash
cd backend
mvn -B spring-boot:run
```

Run tests:

```bash
cd backend
mvn -B test
```

Configuration
- `application.properties` contains placeholders for SERPAPI and Stripe keys. Set as environment variables or in a secure config:
  - `SERPAPI_KEY` / `serpapi.key`
  - `STRIPE_API_KEY` (secret, used server-side)
  - `STRIPE_PUBLISHABLE_KEY` (optional, used by client-side Stripe.js)
  - `STRIPE_WEBHOOK_SECRET` (used to verify webhook signatures in production)

Payment integration
- The codebase includes a REST-based scaffold for Stripe (no stripe-java dependency) and a client-side placeholder in `order-confirmation.html`.
- To enable full Stripe flows, set `STRIPE_API_KEY` and `STRIPE_PUBLISHABLE_KEY` and implement webhook signature verification using the Stripe SDK and `STRIPE_WEBHOOK_SECRET`.

CI / CD
- A GitHub Actions workflow is present to run `mvn test` on pushes.

Removing previous frontend
- The previous React codebase was removed during the server-side rewrite. No `.tsx`/`.jsx` files were found in the repository.

Notes & next steps
- Webhook endpoint is scaffolded at `/payment/webhook` — implement signature verification before using in production.
- Ensure secrets are provided through GitHub Secrets or environment variables and never committed.

If you need a developer environment setup script, Dockerfile, or deployment manifest adjusted to the new backend, I can add them.
>>>>>>> copilot/worktree-2026-07-01T19-46-43
