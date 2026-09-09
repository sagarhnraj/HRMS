# [HRMS Build Phase 1]

Phase 1 of the HRMS build is complete. I have successfully set up the backend and frontend for role-based authentication and stopped for your review, as instructed.

## What has been built:
- **Backend (Spring Boot 3 + Spring Security + JWT)**:
  - Configured MySQL connection in `application.yml`.
  - Created Flyway migrations `V1__init_schema.sql` (auth tables + stub `employees` table) and `V2__seed_data.sql` (roles, permissions, role-permission mappings, and one seeded ADMIN user).
  - Built the entities, repositories, and custom `UserDetailsService`.
  - Implemented stateless JWT auth with `JwtAuthFilter` that validates the token signature, expiration, and ensures it's still present in the database (for logout capability).
  - Built the `/api/auth/login` and `/api/auth/logout` endpoints.

- **Frontend (React + Vite)**:
  - Configured React Router v6 with a `<ProtectedRoute>` wrapper.
  - Implemented `AuthContext` to handle state, JWT decoding, and token persistence in `sessionStorage`.
  - Added Axios interceptors to automatically attach the `Authorization: Bearer <token>` header to requests and handle 401 Unauthorized responses.
  - Built a simple `Login` page.
  - Built a `Dashboard` shell that displays a dynamic navigation menu based strictly on the user's permissions.

## Deviations / Notes for Review:
- **UI Styling:** I kept the frontend extremely minimal using inline styles. No CSS frameworks or heavy UI kits were introduced yet to keep Phase 1 as lean as requested.
- **Tokens Table:** The `jwt_tokens` table is utilized to maintain active session tokens. On logout, the token is deleted from this table, instantly invalidating it on the server-side even before it expires.
- **Maven Wrapper:** Since `mvn` is needed to build the project, please ensure Maven is installed on your local system to run the backend via `mvn spring-boot:run`. 

## How to Test:
A `README.md` file has been added to the root directory `e:\HRMS\README.md` with full setup instructions. The seeded credentials to test the end-to-end auth flow are:
- **Email:** `admin@hrms.local`
- **Password:** `admin123`

Please review the codebase and let me know if everything looks correct, or if you'd like to make any adjustments before we proceed to Phase 2 (Employee Management Service).
