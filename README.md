# MyCloud

MyCloud is an ongoing personal cloud storage project built with Java, Spring Boot, Spring Security, JPA/Hibernate, and PostgreSQL. **Phase 3 is complete.** The current backend manages file metadata and folders.

## Implemented

- Register and log in with Argon2id password hashing and signed JWT access tokens. `GET /api/users/me` returns the authenticated profile.
- Create and list nested folders. Create, list, read, and delete file metadata.
- Scope file and folder access to the owner from the verified JWT. Cross-account resource lookups return `404`. Missing or invalid credentials return `401`.
- Verify authentication, PostgreSQL persistence, and cross-account access with automated tests.

| Method | Endpoint | Access |
| --- | --- | --- |
| GET | `/api/health` | Public |
| POST | `/api/auth/register`, `/api/auth/login` | Public |
| GET | `/api/users/me` | Bearer JWT |
| GET, POST | `/api/folders` | Bearer JWT, owner scope |
| GET, POST | `/api/files` | Bearer JWT, owner scope |
| GET, DELETE | `/api/files/{id}` | Bearer JWT, owner scope |

## Run locally

Requirements: JDK 21 or newer, Maven 3.9, and Docker Compose. Create `mycloud-backend/.env` with your own `MYCLOUD_DB_PASSWORD` and `MYCLOUD_JWT_SECRET`. The JWT secret is a Base64 value from `openssl rand -base64 32`. PostgreSQL uses local port `5433`.

```bash
cd mycloud-backend
set -a
source .env
set +a
docker compose up -d
docker compose exec db pg_isready -U mycloud -d mycloud
mvn clean test
mvn spring-boot:run
```

Once the app starts, call `GET http://localhost:8080/api/health`. Register with `email`, `firstName`, `lastName`, and `password`. Log in with `email` and `password`, then send the returned `accessToken` as `Authorization: Bearer <accessToken>` to the protected endpoints. See [backend setup](mycloud-backend/README.md) for configuration and legacy metadata handling.

## Planned

- **Phase 4:** private S3 object storage and presigned upload/download URLs for file contents.
- **Phase 5:** a minimal Next.js file manager.
- **Phase 6:** an HTTPS deployment with a separate recruiter demo environment.
