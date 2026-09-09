# HRMS Deployment Guide

This document lists all environment variables required to run the HRMS application in the `prod` profile.

## Backend Environment Variables

| Variable | Description | Example Value |
|---|---|---|
| `DB_URL` | JDBC URL for the production MySQL database | `jdbc:mysql://db.prod.internal:3306/hrms_prod?useSSL=false&serverTimezone=UTC` |
| `DB_USERNAME` | Production database username | `hrms_app_user` |
| `DB_PASSWORD` | Production database password | `SuperSecretDbPassword!` |
| `JWT_SECRET` | Strong, random Base64 key used to sign JWTs (MUST be unique to prod and at least 256 bits) | `a8b2c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7A8B9C0D1E2F` |
| `JWT_EXPIRATION_MS` | (Optional) Token lifetime in milliseconds. Defaults to 24 hours | `86400000` |
| `ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins for CORS | `https://hrms.acmecorp.com` |

## Frontend Environment Variables

These variables must be provided at build time (e.g., via `.env.production`).

| Variable | Description | Example Value |
|---|---|---|
| `VITE_API_URL` | The public URL of the deployed backend API | `https://api.hrms.acmecorp.com` |

## Security Notes
*   **HTTPS**: It is assumed that HTTPS termination (TLS) happens at the reverse proxy/load balancer level (e.g., NGINX, AWS ALB) sitting in front of the backend and frontend. The Spring Boot app exposes HTTP on port 8080 internally.
*   **Actuator**: The production profile (`prod`) automatically disables verbose Actuator endpoints and disables Hibernate SQL logging. Only `/actuator/health` and `/actuator/info` are exposed.
