# Oris Backend
**A modular Spring Boot backend for authenticated real-time chat, profile media handling, and notification delivery.**

**Status:** Development

## Tech Stack
- **Kotlin 2.2.21**
- **Spring Boot 4.0.1**
- **Gradle (Kotlin DSL)** with multi-module build (`app`, `user`, `chat`, `notification`, `common`)
- **PostgreSQL** + Spring Data JPA
- **Redis** for caching and rate limiting
- **RabbitMQ** for event-driven communication between modules
- **Spring WebSocket** for real-time chat updates
- **JWT (jjwt 0.13.0)** for stateless authentication
- **Firebase Admin SDK** + **SMTP (Mailtrap)** for push/email notifications
- **Supabase Storage** integration for profile pictures

## Key Features
- **JWT-based authentication** with register, login, refresh, logout, and password flows.
- **Real-time chat** with WebSocket events for message delivery, participant updates, and message deletion.
- **Chat and participant management** including chat creation, join/leave operations, and profile picture updates.
- **Rate limiting** on auth-sensitive operations with Redis-backed strategies.
- **Notification module** for device token registration and event-driven notification handling.
- **Modular architecture** with clear `api`, `service`, `domain`, and `infra` boundaries.

## Architecture
Oris is designed as a **modular monolith**. Each module owns its domain logic while sharing common contracts through the `common` module.

| Module | Responsibility |
|---|---|
| `app` | Application bootstrap, security configuration, Redis/mail/AMQP wiring, and runtime profiles. |
| `user` | Authentication, token lifecycle, email verification, password reset/change, and auth rate limiting. |
| `chat` | Chat lifecycle, chat membership, message operations, and real-time WebSocket communication. |
| `notification` | Device token management plus push/email notification integrations. |
| `common` | Shared JWT service, domain exceptions, event contracts, and messaging configuration. |

Request and event flow follows the same boundary: **Controller -> Service -> Domain -> Infra**. Cross-module communication uses **events** over RabbitMQ and transactional event listeners.

## Quick Start
1. Clone the repository.

```bash
git clone <your-repo-url>
cd oris
```

2. Export required environment variables.

```bash
export POSTGRES_PASSWORD="<postgres-password>"
export REDIS_PASSWORD="<redis-password>"
export RABBITMQ_PASSWORD="<rabbitmq-password>"
export JWT_SECRET_BASE64="<base64-encoded-secret>"
export MAIL_TRAP_PASSWORD="<mailtrap-password>"
export SUPABASE_SERVICE_KEY="<supabase-service-key>"
```

3. Build the project.

```bash
./gradlew clean :app:build
```

4. Run the application (dev profile).

```bash
./gradlew :app:bootRun --args='--spring.profiles.active=dev'
```

The service starts on **http://localhost:8080** by default.

## Environment Configuration
| Variable | Required | Purpose |
|---|---|---|
| `POSTGRES_PASSWORD` | Yes | Password for PostgreSQL datasource (`spring.datasource.password`). |
| `REDIS_PASSWORD` | Yes | Password for Redis (`spring.data.redis.password`). |
| `RABBITMQ_PASSWORD` | Yes | Password for RabbitMQ (`spring.rabbitmq.password`). |
| `JWT_SECRET_BASE64` | Yes | Base64 secret used to sign and validate JWT tokens. |
| `MAIL_TRAP_PASSWORD` | Yes | SMTP password for outbound email operations. |
| `SUPABASE_SERVICE_KEY` | Yes | Service key used by Supabase storage integration. |
| `SUPABASE_URL` | Optional | Overrides Supabase URL (a default value exists in config). |

Minimum local prerequisites:
- **JDK 21**
- **Gradle Wrapper** (`./gradlew`, included)
- Reachable **PostgreSQL**, **Redis**, and **RabbitMQ** instances

## API Highlights
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Creates a new user account. |
| `POST` | `/api/auth/login` | Authenticates a user and returns access/refresh tokens. |
| `POST` | `/api/auth/refresh` | Rotates tokens using a valid refresh token. |
| `POST` | `/api/auth/logout` | Invalidates the provided refresh token session. |
| `POST` | `/api/auth/resend-verification` | Resends account verification email with rate-limit protection. |
| `GET` | `/api/auth/verify` | Verifies email using query parameter `token`. |
| `POST` | `/api/auth/forgot-password` | Starts password reset flow for a user email. |
| `POST` | `/api/auth/reset-password` | Completes password reset using reset token. |
| `POST` | `/api/auth/change-password` | Changes password for the authenticated user. |
| `GET` | `/api/chat` | Returns chats for the authenticated user. |
| `GET` | `/api/chat/{chatId}/messages` | Returns paginated chat messages (`before`, `pageSize`). |
| `GET` | `/api/chat/{chatId}` | Returns a single chat by id for authorized participant. |
| `POST` | `/api/chat` | Creates a new chat with selected participants. |
| `POST` | `/api/chat/{chatId}/add` | Adds one or more users to an existing chat. |
| `DELETE` | `/api/chat/{chatId}/leave` | Removes authenticated user from the chat. |
| `GET` | `/api/participants` | Returns participant by `query` or current user profile when omitted. |
| `POST` | `/api/participants/profile-picture-upload` | Generates upload credentials for profile picture (`mimeType` query). |
| `POST` | `/api/participants/confirm-profile-picture` | Confirms uploaded profile picture URL. |
| `DELETE` | `/api/participants/profile-picture` | Deletes current user profile picture. |
| `DELETE` | `/api/messages/{messageId}` | Deletes a message owned/accessible by the requester. |
| `POST` | `/api/notification/register` | Registers a mobile/web device token for push notifications. |
| `DELETE` | `/api/notification/{token}` | Unregisters a previously stored device token. |
| `WS` | `/websocket/chat` | Real-time chat socket endpoint (Authorization header required). |

## Contribution
Contributions are welcome. Open an issue for bug reports or feature proposals, then submit a focused pull request with tests and clear commit messages.
