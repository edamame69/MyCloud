# MyCloud

MyCloud is an ongoing personal cloud storage project built with Java, Spring Boot, Spring Security, JPA/Hibernate, and PostgreSQL. **Phase 3 learning checkpoint: before file and folder ownership.** The current backend manages file metadata and folders.

## Implemented

- Register and log in with Argon2id password hashing and signed JWT access tokens. `GET /api/users/me` returns the authenticated profile.
- Create and list nested folders. Create, list, read, and delete file metadata.
- Verify authentication and PostgreSQL persistence with automated tests.

| Method | Endpoint | Access |
| --- | --- | --- |
| GET | `/api/health` | Public |
| POST | `/api/auth/register`, `/api/auth/login` | Public |
| GET | `/api/users/me` | Bearer JWT |
| GET, POST | `/api/folders` | Local development only |
| GET, POST | `/api/files` | Local development only |
| GET, DELETE | `/api/files/{id}` | Local development only |

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

Once the app starts, call `GET http://localhost:8080/api/health`. Register with `email`, `firstName`, `lastName`, and `password`. Log in with `email` and `password`, then send the returned `accessToken` as `Authorization: Bearer <accessToken>` to the protected endpoints. See [backend setup](mycloud-backend/README.md) for configuration. Use this checkpoint locally while file and folder access controls are being developed.

## Planned

- **Phase 3:** protect file and folder endpoints and enforce ownership for metadata access.

- **Phase 4:** private S3 object storage and presigned upload/download URLs for file contents.
- **Phase 5:** a minimal Next.js file manager.
- **Phase 6:** an HTTPS deployment with a separate recruiter demo environment.
