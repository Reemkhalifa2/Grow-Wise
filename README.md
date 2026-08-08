# Investment Goal Management Platform

A full-stack web app for setting financial goals and building AI-assisted investment plans to reach them. Users track goals, get an AI-generated asset allocation for their monthly investment, monitor their portfolio, and manage daily investing habits (to-dos, streaks). Admins manage the asset catalog and see platform-wide stats.

## Tech stack

**Backend** — `backend/`
- Java 21, Spring Boot 4.1
- Spring Data JPA + Hibernate, MySQL
- Spring Security with stateless JWT auth (`jjwt`)
- Google ID token verification for "Sign in with Google"
- Spring AI (`spring-ai-starter-model-google-genai`) for AI-assisted investment allocation suggestions, backed by the Gemini API


**Frontend** — `Frontend/`
- Angular 21 (standalone components)
- Angular SSR/Express server support

## Features

- Email/password and Google sign-in, JWT-based sessions
- Financial goal creation and tracking
- AI-assisted investment plan suggestions (Gemini) with manual override of asset allocations
- Portfolio and investment tracking
- Admin dashboard and asset catalog management
- To-do list and streak tracking for investing habits

## Project structure

```
.
├── backend/     Spring Boot API (port 8080)
├── Frontend/    Angular app (port 4200)
└── docker-compose.yml
```

## Prerequisites

- JDK 21+ (a JDK, not just a JRE — the Maven wrapper needs `JAVA_HOME` set)
- Node.js + npm
- MySQL running locally (or via Docker)

## Backend setup

1. Create `backend/.env` (gitignored) with:

   ```env
   DB_URL=jdbc:mysql://localhost:3306/InvestmentGoalManagementDB?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   DB_USERNAME=root
   DB_PASSWORD=root
   GEMINI_API_KEY=your-gemini-api-key
   GOOGLE_CLIENT_ID=your-google-oauth-client-id
   ```

   - `GEMINI_API_KEY` — free key from [Google AI Studio](https://aistudio.google.com/apikey), used for AI investment suggestions.
   - `GOOGLE_CLIENT_ID` — OAuth 2.0 Web client ID from Google Cloud Console; must match the client ID the Angular app initializes Google Identity Services with.

2. Run it:

   ```bash
   cd backend
   ./mvnw.cmd spring-boot:run   # Windows
   ./mvnw spring-boot:run       # macOS/Linux
   ```

   Hibernate auto-creates/updates the schema (`spring.jpa.hibernate.ddl-auto=update`) against the MySQL database named in `DB_URL`.

   API runs at `http://localhost:8080`. Swagger UI is available once springdoc is on the classpath (default path `/swagger-ui.html`).

## Frontend setup

```bash
cd Frontend
npm install
npm start   # ng serve
```

App runs at `http://localhost:4200` and expects the backend at `http://localhost:8080`.

## Running with Docker

```bash
docker compose up --build
```

Builds and runs both services — backend on `8080`, frontend on `4200`. Set `GEMINI_API_KEY` in your shell environment before running so it's passed through to the backend container.

## Notes

- CORS and JWT are configured for local development (`http://localhost:4200` origin, hardcoded API base URLs in the Angular services) — update these before deploying anywhere else.
- `backend/.env` holds real secrets and is gitignored; never commit it.
