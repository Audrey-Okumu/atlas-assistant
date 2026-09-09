# Atlas Assistant — Backend

An AI-powered WhatsApp assistant that connects to a user's Google account to help them manage email and calendar events. Ask it a question on WhatsApp — "Any important emails today?", "Do I have meetings tomorrow?" — and it answers using real, live data from Gmail and Google Calendar. It also proactively reminds you before meetings and flags important emails, without you having to ask.

**Live API:** `https://atlas-assistant-kq2s.onrender.com`
**Frontend:** [atlas-assistant-web](https://github.com/Audrey-Okumu/atlas-assistant-web)

---

## What it does

- Sign up with email/password or Google OAuth2
- Securely connect a Google account (read-only Gmail and Calendar access)
- Ask natural-language questions and get AI-generated answers grounded in real Gmail and Calendar data
- Receive proactive WhatsApp reminders before upcoming meetings
- Get notified of important emails automatically, without repeat spam
- All conversation happens over WhatsApp, or through a companion web dashboard

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| Build tool | Maven (with wrapper) |
| Database | PostgreSQL, accessed via Spring Data JPA / Hibernate |
| Auth | Spring Security, JWT (jjwt), Google OAuth2 |
| AI | Groq API (Llama / GPT-OSS models) |
| Messaging | Twilio WhatsApp API |
| Calendar / Email | Google Calendar API, Gmail API |
| Containerization | Docker, Docker Compose (multi-stage build) |
| Testing | JUnit 5, Mockito |
| Deployment | Render (Docker-based Web Service + managed PostgreSQL) |

## Architecture

The project follows a standard layered architecture:

```
controller/    → REST endpoints, HTTP concerns only
service/       → business logic (Gmail, Calendar, AI, WhatsApp, reminders)
repository/    → Spring Data JPA interfaces
model/         → JPA entities
config/        → Spring Security, JWT filter, CORS
```

Authentication uses a custom `JwtAuthFilter` sitting alongside Spring Security's OAuth2 Client support — a user can register with a password, sign in with Google, or both, and either path results in the same application-issued JWT used for all subsequent API calls.

A scheduled job (`ReminderService`, running every 5 minutes via `@Scheduled`) checks each connected user's calendar and inbox, and sends a WhatsApp message only the first time a given event or email qualifies — deduplicated against a `sent_notifications` table keyed on Google's own event/message IDs.

## Getting started locally

### Prerequisites
- Java 21 (Eclipse Temurin recommended)
- Maven Wrapper (bundled — no separate Maven install needed)
- PostgreSQL 18, or Docker
- A Google Cloud project with the Gmail API and Calendar API enabled, and OAuth2 credentials
- A Groq API key
- A Twilio account (Sandbox is fine for development)

### 1. Clone and configure

```bash
git clone git@github.com:Audrey-Okumu/atlas-assistant.git
cd atlas-assistant
```

Create `src/main/resources/application-local.properties` (git-ignored) using `application-local.properties.example` as a template, and fill in:

```properties
spring.datasource.username=postgres
spring.datasource.password=your_local_db_password

spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...

groq.api.key=...
twilio.account-sid=...
twilio.auth-token=...
twilio.whatsapp-number=whatsapp:+14155238886
twilio.content-sid=...
```

### 2. Run with Docker Compose 

```bash
docker compose up --build
```

This starts both the app and a PostgreSQL container, networked together, with schema created automatically on first run.

### 3. Or run directly with the Maven wrapper

```bash
./mvnw spring-boot:run      # Mac/Linux
.\mvnw.cmd spring-boot:run  # Windows
```

Requires a locally running PostgreSQL instance matching your `application-local.properties`.

### 4. Run the tests

```bash
./mvnw test
```

## Key API endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register with email, password, name, and phone number |
| POST | `/auth/login` | Log in, returns a JWT |
| GET | `/oauth2/authorization/google` | Start Google OAuth2 connection flow |
| POST | `/users/phone-number` | Link a WhatsApp number to the authenticated account |
| GET | `/emails/recent` | Fetch the authenticated user's recent Gmail subjects |
| GET | `/calendar/upcoming` | Fetch the authenticated user's upcoming Calendar events |
| GET | `/assistant/ask?question=...` | Ask a natural-language question, answered by AI using live Gmail/Calendar data |
| POST | `/webhook/whatsapp` | Twilio webhook — receives and replies to WhatsApp messages |

All endpoints except `/auth/**`, `/oauth2/**`, `/login/**`, `/webhook/**`, `/`, and `/health` require a valid JWT via `Authorization: Bearer <token>`.

## Deployment

Deployed on [Render](https://render.com) as a Docker-based Web Service, with a managed PostgreSQL database on the same platform. Configuration (database credentials, API keys, OAuth secrets) is supplied entirely via environment variables — no secrets are committed to source control.

## Known limitations

- Running on Render's free tier, the application sleeps after ~15 minutes of inactivity, which can delay or skip scheduled background reminders until the next incoming request wakes it.
- WhatsApp messaging currently runs on Twilio's Sandbox, which requires each tester to send a join code before they can interact with the assistant. A registered WhatsApp Sender would remove this step.

## License

Personal/portfolio project.
