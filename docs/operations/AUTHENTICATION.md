# Local authentication and machine boundary

The browser UI uses a server-side session created by `/signin`. PINs must contain exactly six digits and are stored only as a `PasswordEncoder` hash. Five consecutive failures lock the account for 15 minutes. `BLOCKED` accounts never authenticate.

Bootstrap the first `OWNER` once with `BOOTSTRAP_OWNER_NAME` and `BOOTSTRAP_OWNER_PIN`. The application writes only the encoded PIN. Remove both environment variables immediately after the first successful start.

Browser authorization is: `USER` — Plan–Fact, Comments, Report; `ADMIN` — USER access plus Settings; `OWNER` — ADMIN access plus the future Users area. Navigation visibility is convenience only; the same rules are enforced for URLs and APIs.

`OWNER` manages local accounts at `/page/users`: create accounts, change display name or role, rotate PIN, and block or activate access. The server prevents blocking or demoting the last active OWNER. Role and status changes take full effect for a user's next sign-in; revoke an existing session operationally if immediate eviction is required.

In the `prod` profile, every non-GET `/users` operation is written to the critical-change audit log as `user-security-change`; PIN values are never logged.

`/api/signal/**` is a separate machine-channel boundary and is deliberately excluded from form login and CSRF. The current HTTP simulation adapter exists only under the `test` profile. Before enabling any production HTTP signal adapter, add machine authentication (for example mTLS or a rotated service credential); never reuse a human session or PIN. The internal ADAM Modbus adapter does not use HTTP authentication and remains unchanged.
