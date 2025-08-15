# Vehicle Inventory System

A backend-focused vehicle inventory manager for a small dealership: REST API
(Spring Boot, Spring Security, JPA/Hibernate, Supabase Postgres + Storage) plus
a minimal, functional admin UI — add/edit vehicles, upload photos, filter and
search, and a one-screen dashboard.

**This is a portfolio/demo build, not a record of a specific past client
project.** It was built to have something concrete to demo and attach to
proposals after the artifacts from an earlier, similarly-scoped engagement (for
a client referred to in a bid only as prior experience) were no longer
available. There is no real dealership data anywhere in this repo — the only
"vehicles" are whatever you enter while trying it out.

See [`docs/case-study.html`](docs/case-study.html) — also published as a
formatted page, ask for the link — for a bid-ready write-up (brief-to-delivery
traceability, architecture diagram, API surface, and the bugs actually found
during testing). The full raw build/iteration log, plus the original brief
this was built against, is in [`docs/build-log.txt`](docs/build-log.txt).

## Stack

- Java 17, Spring Boot 3.3
- Spring Security with stateless JWT auth (single admin role — this is an
  internal tool, not a multi-tenant SaaS)
- Spring Data JPA / Hibernate, PostgreSQL (Supabase-hosted)
- Supabase Storage for vehicle photos, called directly over its REST API
- Flyway for schema migrations (production profile)
- springdoc-openapi (Swagger UI) for the REST API
- Plain HTML/CSS/JS admin frontend served as Spring Boot static resources —
  no build step, no framework, because the brief asked for functional over
  fancy

## Project layout

```
src/main/java/com/dealerhub/inventory/
├── domain/       Vehicle, VehiclePhoto, AdminUser entities + enums
├── repository/   Spring Data repositories + JPA Specifications for search
├── dto/          Request/response shapes — entities never leave the service layer
├── mapper/       Entity <-> DTO mapping
├── service/      Business logic (VehicleService, DashboardService, AuthService,
│                 SupabaseStorageService)
├── security/     JWT issuing/validation, the auth filter, UserDetailsService
├── web/          REST controllers
├── exception/    Typed exceptions + a single @RestControllerAdvice
└── seed/         Creates the first admin login on a fresh database

src/main/resources/
├── db/migration/     Flyway SQL (production/Supabase schema)
├── static/           The admin UI (plain HTML/CSS/JS)
└── application*.yml  Config, split by profile (dev = H2, prod = Supabase)
```

## Running it locally (no Supabase project needed for the database)

The `dev` profile (active by default) uses an in-memory H2 database, so you can
run the whole thing with nothing but a JDK:

```bash
export APP_ADMIN_SEED_PASSWORD=ChangeMe123!
mvn spring-boot:run
```

Open `http://localhost:8080` and sign in with `admin` / `ChangeMe123!` (or
whatever you set `APP_ADMIN_SEED_PASSWORD` to — that seed only runs once,
against an empty `admin_users` table).

Photo upload still needs a real Supabase project, since Storage isn't
something H2 can stand in for — see below. Without it configured, every other
feature (CRUD, search, dashboard) works fine; only the photo upload button
will fail.

## Setting up Supabase (for photos, and for the real production database)

1. Create a project at [supabase.com](https://supabase.com).
2. **Storage:** create a bucket named `vehicle-photos` (or anything — just
   match `SUPABASE_STORAGE_BUCKET`) and mark it **public**. This app renders
   photos with plain `<img src>` tags, so it relies on public read access
   rather than signed URLs.
3. **Database:** Project Settings → Database → Connection string, and convert
   it to JDBC form for `SUPABASE_DB_URL`:
   `jdbc:postgresql://<host>:5432/postgres`.
4. **Keys:** Project Settings → API. Use the **service role** key server-side
   only (`SUPABASE_SERVICE_ROLE_KEY`) — never ship it to the frontend. This
   app never does; the browser only ever talks to this Spring Boot backend.

Environment variables (production profile):

| Variable | Purpose |
|---|---|
| `SPRING_PROFILES_ACTIVE=prod` | Switches to the Supabase-backed profile |
| `SUPABASE_DB_URL` / `SUPABASE_DB_USERNAME` / `SUPABASE_DB_PASSWORD` | Postgres connection |
| `SUPABASE_URL` | e.g. `https://xyzcompany.supabase.co` |
| `SUPABASE_SERVICE_ROLE_KEY` | Storage API auth |
| `SUPABASE_STORAGE_BUCKET` | Defaults to `vehicle-photos` |
| `APP_JWT_SECRET` | HMAC signing key for issued JWTs — set a real random 32+ byte value |
| `APP_JWT_EXPIRATION_SECONDS` | Defaults to 28800 (8 hours) |
| `APP_ADMIN_SEED_USERNAME` / `APP_ADMIN_SEED_PASSWORD` | First admin login, created once on an empty database |
| `APP_CORS_ALLOWED_ORIGINS` | Only relevant if the UI is ever split onto its own origin |

## API

Full interactive docs at `/swagger-ui.html` once the app is running. Shape,
briefly:

| Endpoint | Purpose |
|---|---|
| `POST /api/auth/login` | Returns a JWT |
| `GET /api/vehicles` | Paginated search — `brand`, `model`, `year`, `status`, `condition`, `minPrice`, `maxPrice`, `q` (free text) |
| `POST /api/vehicles` / `PUT /api/vehicles/{id}` | Create/update |
| `DELETE /api/vehicles/{id}` | Deletes the vehicle and its stored photos |
| `POST /api/vehicles/{id}/photos` | Multipart upload, one or more files |
| `DELETE /api/vehicles/{id}/photos/{photoId}` | Removes one photo (from Storage too) |
| `PUT /api/vehicles/{id}/photos/{photoId}/primary` | Sets the list/dashboard thumbnail |
| `GET /api/dashboard/summary` | Counts by status, inventory value, revenue/profit to date |
| `GET /api/lookups/statuses` / `/conditions` | Enum values, so the UI never hard-codes them |

Every `/api/**` route except `/api/auth/login` requires `Authorization: Bearer
<token>`.

## Testing

```bash
mvn test
```

- `VehicleServiceTest` — unit tests against mocked repositories (duplicate VIN
  rejection, not-found handling, photo cleanup on delete)
- `VehicleControllerIT` / `AuthControllerIT` — full-context MockMvc tests
  against an in-memory H2 database, covering auth, validation, search
  filtering, and the delete-cascades-to-photos path
- Photo storage is mocked in the controller tests — nothing here makes a real
  network call to Supabase

## Notes for a technical reviewer

- VIN is validated against the real 17-character format (excluding I/O/Q) and
  enforced unique at both the application and database level.
- Secrets (Supabase service role key, JWT signing secret, admin seed password)
  are read from environment variables only — nothing sensitive is committed,
  and `application-dev.yml`'s H2 credentials are meaningless outside a
  throwaway in-memory database.
- `ddl-auto` is `update` in dev (fast iteration against throwaway H2) and
  `validate` in prod, with Flyway owning the real schema — the two profiles
  intentionally diverge here rather than pretending one setting fits both.
- The frontend is deliberately plain: no bundler, no framework, straight
  `fetch()` calls. That was the brief ("functionality is much more important
  than design"), not a shortcut.
