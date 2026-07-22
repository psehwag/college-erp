# CLAUDE.md — Smart College ERP v2

Microservice ERP: Spring Boot backend + React frontend. Roles: ADMIN, FACULTY, STUDENT, PARENT.

## 1. Tech stack
- **Backend**: Java 17, Spring Boot 3.2.0, Spring Cloud 2023.0.0 (Netflix Eureka + Gateway), Spring Data JPA, Spring Security, Spring Kafka, MySQL 8. Maven multi-module (`com.erp:college-erp-parent:2.0.0`). **No Lombok.**
- Libs: jjwt 0.11.5 (JWT), bytedeco javacv-platform 1.5.9 (face recognition), springdoc-openapi 2.3.0 (Swagger).
- **Frontend**: React 18, Create React App (react-scripts 5), react-router-dom 6, axios, recharts, react-hot-toast, react-webcam, date-fns, clsx. **Plain JS, no TypeScript.**
- Kafka runs in KRaft mode (no Zookeeper). Orchestrated via Docker Compose.

## 2. Folder structure
- `college-erp-backend/` — Maven parent + 4 modules:
  - `eureka-server/` — service discovery (:8761)
  - `api-gateway/` — routing, CORS, JWT validation (:8080); `filter/JwtAuthFilter`, `util/JwtUtil`
  - `college-erp-service/` — all business logic (:8081, db `erp_main`). Package-by-feature under `com.erp.*`: `auth, student, faculty, parent, course, attendance, marks, admin, face`, plus `common` (config, dto, exception, util)
  - `notification-service/` — Kafka consumer + email (:8082, db `erp_notifications`)
- `college-erp-frontend/src/` — `components/` (common, layout), `context/AuthContext.jsx`, `hooks/`, `pages/` (admin, faculty, student, parent), `services/api.js`

## 3. Key commands
- Backend build: from `college-erp-backend/` run `mvn clean package`; tests `mvn test`.
- Run everything: from `college-erp-backend/` run `docker-compose up -d` (MySQL, Kafka, all 4 services). `docker-compose down -v` wipes data.
- Frontend: from `college-erp-frontend/` run `npm install`, `npm start` (dev :3000, proxies to :8080), `npm run build`, `npm test`.
- No linter is configured in this project.

## 4. Hard rules
- **Never add Lombok** — hand-write constructors, getters, and setters (see `ApiResponse.java`).
- RBAC is **not** annotation-based. Enforce it in controllers with `common/util/RoleGuard` (`requireAdmin`, `requireAnyRole`, `requireOwnerOrAdmin`), reading the `X-User-Role` / `X-Reference-Id` headers. `SecurityConfig` permits all — business services trust the gateway's `X-User-*` headers.
- Only `/api/auth/login` and `/api/auth/refresh-token` are public. Everything else goes through `JwtAuthFilter`.
- In `api-gateway/application.yml`, specific routes (e.g. `/api/notifications/**`) **must** be listed before the `/api/**` catch-all, or they hit the wrong service.
- Wrap every controller response in `ApiResponse.success(...)` / `ApiResponse.error(...)`.
- The `jwt.secret` in `api-gateway` and `college-erp-service` application.yml must stay identical.
- DB schema is `ddl-auto: update` (no migration tool); reference data is created by `common/config/DataSeeder`. Don't add Flyway/Liquibase.
- Frontend: call the API only through the domain objects in `services/api.js` (`studentAPI`, `authAPI`, …); never use raw axios in components. Tokens live in `localStorage` (`accessToken`, `refreshToken`, `user`).

## 5. Coding conventions
- Backend package-by-feature, each with `controller / dto / entity / repository / service` sub-packages.
- Constructor injection into `final` fields (no `@Autowired` on fields).
- DTOs are nested static classes per feature: `StudentDto.CreateRequest`, `.Response`, `.Summary`.
- Javadoc on controller methods states the allowed roles.
- Validate request bodies with `@Valid` + `jakarta.validation`.
- Frontend: functional components + hooks; `useAuth()` for auth state; named exports; API modules grouped by domain with arrow-function methods.
- Indentation: 4 spaces (Java), 2 spaces (JS/JSX).
