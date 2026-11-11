# MyCloud backend — local development

The application requires `MYCLOUD_DB_PASSWORD` and `MYCLOUD_JWT_SECRET` in its process environment. The ignored local `.env` contains the development values. Do not commit or print them. Spring Boot does not automatically load `.env`: export its variables when running Maven, or provide both variables in IntelliJ's run/test configuration.

`MYCLOUD_JWT_SECRET` must be Base64 encoding of at least 32 cryptographically random bytes. It is a signing key, not a user password or the database password. Keep the same key across restarts; changing it invalidates tokens issued with the previous key. There is no default application signing key. For a fresh environment, generate a new random key locally and store it securely.

JWT issuance uses HS256 through Spring Security's `JwtEncoder`. Claims are `sub` (user UUID), `iat`, `exp` (30 minutes after issue), and `iss` (`https://mycloud.local`, an identifier only). No request is sent to the issuer URL. There is no refresh token in this slice.

Registration and login are implemented, including the learner-written `AuthService` methods. Login finds the user by email, checks the supplied password with `PasswordEncoder.matches`, and returns a signed access token. Unknown email and wrong password return the same 401 message. `AuthLoginTest` runs the real database-backed login checks in the default test suite. IntelliJ's bundled Maven is the working Maven installation for this project.

The Spring Security filter chain requires `Authorization: Bearer <accessToken>` for `/api/users/**`, `/api/files/**`, and `/api/folders/**`. The decoder checks the HS256 signature, issuer, required expiration, optional not-before time, and UUID subject using the local signing key. The application issues and verifies tokens on the same clock with no expiration leeway.

`GET /api/users/me` returns `id`, `email`, `firstName`, and `lastName`. The controllers take the user ID from the verified JWT subject. The services confirm the account exists and scope file and folder queries to that owner, including parent-folder selection and name conflicts. Missing or invalid tokens and tokens whose account is missing return 401. A missing resource or a resource outside the account's ownership returns 404.

The local Hibernate schema update adds `owner_id` foreign keys to `files` and `folders`. Existing rows without an owner remain in PostgreSQL and are excluded from account queries. Create fresh metadata with a signed-in account for the local demo. `ResourceOwnershipTest` covers missing tokens, account isolation, cross-account file deletion and folder selection, and legacy rows.
