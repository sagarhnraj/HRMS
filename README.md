# HRMS Phase 1

This repository contains Phase 1 of the HRMS application: Role-based Authentication.

## Prerequisites
- Java 17+
- Maven
- Node.js 18+
- MySQL 8

## Database Setup
1. Open MySQL shell or your favorite MySQL client.
2. Run the following command to create the database:
   ```sql
   CREATE DATABASE hrms_db;
   ```
3. The application will automatically create the tables and insert seed data via Flyway on startup. It connects as `root` with password `password`. Modify `backend/src/main/resources/application.yml` if your local MySQL credentials differ.

## Running the Backend
1. Navigate to the `backend` directory.
2. Run the Spring Boot application using Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   *Note: If you have maven installed globally, you can also run `mvn spring-boot:run`.*
   The backend will start on `http://localhost:8080`.

## Running the Frontend
1. Navigate to the `frontend` directory.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
   The frontend will start on `http://localhost:5173`.

## Testing the Application
1. Open your browser and go to `http://localhost:5173`.
2. You will be redirected to the login page.
3. Login using the seeded admin credentials:
   - **Email:** `admin@hrms.local`
   - **Password:** `admin123`
4. On successful login, you will see the dashboard with a nav menu corresponding to your roles.

## Deviations from Spec
- Kept the UI extremely minimal as requested. Used inline styles instead of CSS files to keep the files compact.
- No other major deviations from the specifications! Let me know if everything looks good, and we can proceed to Phase 2.
