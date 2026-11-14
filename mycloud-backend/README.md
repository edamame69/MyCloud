# MyCloud backend — local development

The application requires `MYCLOUD_DB_PASSWORD` and `MYCLOUD_JWT_SECRET` in its process environment. The ignored local `.env` contains the development values. Do not commit or print them. Spring Boot does not automatically load `.env`: export its variables when running Maven, or provide both variables in IntelliJ's run/test configuration.

`MYCLOUD_JWT_SECRET` must be Base64 encoding of at least 32 cryptographically random bytes. It is a signing key, not a user password or the database password. Keep the same key across restarts; changing it invalidates tokens issued with the previous key. There is no default application signing key. For a fresh environment, generate a new random key locally and store it securely.

JWT issuance uses HS256 through Spring Security's `JwtEncoder`. Claims are `sub` (user UUID), `iat`, `exp` (30 minutes after issue), and `iss` (`https://mycloud.local`, an identifier only). No request is sent to the issuer URL. There is no refresh token in this slice.

Registration and login are implemented, including the learner-written `AuthService` methods. Login finds the user by email, checks the supplied password with `PasswordEncoder.matches`, and returns a signed access token. Unknown email and wrong password return the same 401 message. `AuthLoginTest` runs the real database-backed login checks in the default test suite. IntelliJ's bundled Maven is the working Maven installation for this project.

JWT verification is now wired through a Spring Security filter chain scoped to `/api/users/**`. The decoder checks HS256 signature, issuer, required expiration, optional not-before time, and a UUID subject, using the same local signing key with no network requests. This application currently issues and verifies tokens on the same clock, with no expiration leeway.

`GET /api/users/me` requires a valid Bearer JWT. The controller reads the verified `sub` and the service uses it to load the user from PostgreSQL. The response contains `id`, `email`, `firstName`, and `lastName`. Missing or invalid tokens and tokens whose user is missing return the same 401 response. `UserSecurityTest` and `UserMeTest` exercise these paths in the default test suite. File and folder ownership checks are a subsequent slice.
