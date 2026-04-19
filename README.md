# QR Backend

Spring Boot backend for a QR-code service with companies, QR landing pages, action buttons, form submissions, JWT auth, PostgreSQL, Flyway, and MailHog for local email testing.

## Requirements

- Java 21+
- Maven 3.9+
- Docker Desktop or compatible Docker runtime

## Run Locally

Start dependencies:

```bash
docker compose up -d
```

Run the API from IntelliJ IDEA's Maven tool window, or with a local Maven install:

```bash
mvn spring-boot:run
```

The API starts on:

```text
http://localhost:8080
```

MailHog UI:

```text
http://localhost:8025
```

PostgreSQL is exposed on host port `5433` to avoid clashing with a local Postgres install:

```text
host: localhost
port: 5433
database: qr_backend
username: qr_user
password: qr_password
```

## Demo Login

The local profile creates a demo admin user if it does not exist:

```text
email: admin@example.com
password: password
```

Override with environment variables:

```bash
APP_ADMIN_EMAIL=you@example.com APP_ADMIN_PASSWORD=secret mvn spring-boot:run
```

## Useful Calls

Login:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"password"}'
```

Public demo QR:

```bash
curl -s http://localhost:8080/api/public/q/demo
```

Health:

```bash
curl -s http://localhost:8080/health
```

## Frontend Integration

Use a bearer token from `/api/auth/login` for admin endpoints:

```text
Authorization: Bearer <token>
```

Public endpoints do not require auth:

```text
GET  /api/public/q/{slug}
POST /api/public/q/{slug}/actions/{actionId}/click
POST /api/public/q/{slug}/submit-form
```

Admin endpoints:

```text
GET  /api/companies
POST /api/companies
PUT  /api/companies/{companyId}
GET  /api/companies/{companyId}/qr-codes
POST /api/companies/{companyId}/qr-codes
GET  /api/companies/{companyId}/qr-codes/{qrCodeId}
PUT  /api/companies/{companyId}/qr-codes/{qrCodeId}
GET  /api/companies/{companyId}/qr-codes/{qrCodeId}/png
```

## Deploy To Railway

The project includes a root-level `Dockerfile`, so Railway will build it as a Docker service.

Recommended production layout:

```text
Railway
  qr-backend service
  PostgreSQL service

Vercel or Netlify
  React frontend
```

Create the Railway project:

1. Push this repository to GitHub.
2. In Railway, create a new project.
3. Add a PostgreSQL database service.
4. Add a new service from the GitHub repository.
5. Make sure the backend service uses the root `Dockerfile`.
6. Generate a public domain for the backend service.

Add these variables to the backend service:

```env
SPRING_PROFILES_ACTIVE=prod
APP_PUBLIC_BASE_URL=https://your-frontend-domain.com
APP_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
APP_MAIL_FROM=QR Service <noreply@your-domain.com>
APP_ADMIN_EMAIL=admin@your-domain.com
APP_ADMIN_PASSWORD=replace-with-a-long-one-time-password
JWT_SECRET=replace-with-at-least-32-random-characters
SMTP_HOST=smtp.your-provider.com
SMTP_PORT=587
SMTP_USERNAME=your-smtp-username
SMTP_PASSWORD=your-smtp-password
```

Railway PostgreSQL provides these variables automatically. Reference them from the backend service or copy them from the PostgreSQL service:

```env
PGHOST=...
PGPORT=...
PGDATABASE=...
PGUSER=...
PGPASSWORD=...
```

The production profile builds the JDBC URL from those `PG*` variables:

```text
jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}?sslmode=require
```

After deploy, check:

```text
https://your-backend-domain/health
https://your-backend-domain/actuator/health
https://your-backend-domain/api/public/q/demo
```

Set the frontend API base URL to the Railway backend domain:

```env
VITE_API_BASE_URL=https://your-backend-domain
```
