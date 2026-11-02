# OAuth 2.0 and OpenID Connect provider

OneDev uses the bundled Nimbus OAuth 2.0 / OpenID Connect SDK for request parsing,
responses, PKCE, discovery, token generation and JWT signing. Applications use the
authorization code flow with S256 PKCE. Implicit, password, client credentials,
device authorization and dynamic client registration are not supported.

## Provider signing key

A signing key is generated automatically when the provider setting is first
initialized, enabling the provider by default. Administrators can manage the key
under **Administration → Keys → OAuth Signing Key**. When the key is deleted, the
provider endpoints and application pages are unavailable, and the **OAuth
Applications** and **Authorized Applications** account menu items are hidden.

The same page can regenerate or delete the key. Both actions revoke existing
authorizations while preserving registered applications. Regeneration replaces
the public signing key; clients must reload the JWKS and obtain fresh consent.
Deleting the key disables the provider until a new key is generated. The private
key is stored in the database and is never displayed. Restarting the server
preserves existing keys and does not regenerate deleted keys.

## Register an application

Use **OAuth Applications** in your account menu to register and manage applications.
Administrators can register shared integrations under their own accounts. The owner
manages the registration; any user can sign in through the application and authorize
access to their own projects. Every user must give consent, and access is limited
to that user's permissions.

Click **Add New** to open the registration page. After saving, the application
opens on its own settings page, where you can copy its client ID and newly generated
secret. Click an application name in the list to return to its settings.

Register a name, homepage and exact redirect URIs. HTTPS is required except for
loopback HTTP callbacks (`localhost`, `127.0.0.1`, `[::1]`) and reverse-domain native
application schemes such as `com.example.app:/callback`. Native schemes are only
allowed for public clients. Wildcards, fragments and embedded credentials are
rejected. Public clients using HTTP callbacks to `127.0.0.1` or `[::1]` may choose
an available port at runtime, as required by
[RFC 8252](https://www.rfc-editor.org/rfc/rfc8252.html#section-7.3). All other URI components
must match exactly. Confidential clients and `localhost` callbacks require an
exact port match. Code exchange must use the complete URI, including the port,
from the authorization request.

Use a confidential client for a server application that can keep a secret; use a
public client for a desktop, mobile or browser application. Client secrets are
shown when generated and stored as hashes. Rotating a secret revokes the
application's existing authorizations. Both client types must use PKCE.

## Endpoints

Endpoints are relative to the configured OneDev server URL, which is the issuer.
Use HTTPS in production.

| Purpose | Endpoint | Method |
| --- | --- | --- |
| OAuth discovery | `/.well-known/oauth-authorization-server` | GET |
| OIDC discovery | `/.well-known/openid-configuration` | GET |
| Authorization and consent | `/~oauth/authorize` | GET |
| Code exchange and refresh | `/~oauth/token` | POST |
| Revocation | `/~oauth/revoke` | POST |
| User information | `/~oauth/userinfo` | GET or POST |
| Public signing keys | `/~oauth/jwks` | GET |

Token and revocation requests use `application/x-www-form-urlencoded` bodies.
Confidential clients use `client_secret_basic` or `client_secret_post`; public
clients send `client_id` without a secret. Send one authentication method per
request. API and userinfo calls use `Authorization: Bearer <access_token>`.
Browser clients should perform these calls through their own backend; cross-origin
token requests are not enabled.

## Scopes and project permissions

Discovery and the registration screen list supported scopes and role descriptions.

| Scope | Access |
| --- | --- |
| `openid` | An RS256-signed ID token identifying the OneDev user |
| `profile` | Name and username through userinfo |
| `email` | Primary email address and its verification status through userinfo |
| `offline_access` | Explicitly requests continued access while the user is away |
| `role:<normalized-name>` | The named role's permissions on projects selected during consent |

At least one supported scope is required. Role scopes use lowercase role names
with spaces replaced by underscores. Special and non-ASCII characters are UTF-8
percent-encoded to remain within one scope token. For example, Code Reader has
scope `role:code_reader`. Use the exact scope values listed in discovery. Pass
`openid profile role:code_reader` as the scope value to your OAuth client library;
normal request encoding still applies.

Renaming a role changes its scope if the normalized name changes. Clients must
request the new scope and obtain fresh consent. Existing grants lose that role's
project authorizations on their next token exchange or refresh. ID-based scopes
from earlier versions must also be replaced with name-based scopes and fresh
consent.

The user selects projects they have permission to manage for the requested roles,
matching manual access-token authorization. The server checks this permission
again when consent is submitted. Every requested role applies to
each selected project and its children that inherit settings. The consent page
shows the role's detailed permissions. Each project-role authorization applies
only while the token owner has permission to manage the authorized project,
matching manual access-token authorization. Losing that permission removes access
through that authorization, even if the owner retains read access. OAuth tokens
also receive default project permissions available to logged-in users, just like
regular access tokens, including for projects not selected during consent.
Role edits affect existing authorizations, as with personal access tokens.

OAuth grants reuse OneDev's project-role access-token authorizations. They appear
under **Authorized Applications** and cannot be expanded through the personal
access-token editor or REST API. System administration and account administration
permissions are never delegated through role scopes.

## Authorization and token lifecycle

1. Generate a fresh PKCE verifier and its S256 challenge. Redirect to the
   authorization endpoint with `response_type=code`, `client_id`, the exact
   `redirect_uri`, `scope`, `state`, `code_challenge` and
   `code_challenge_method=S256`. Include `nonce` for OIDC.
2. OneDev signs the user in and asks for consent. Validate the returned `state`.
3. Exchange the code with `grant_type=authorization_code`, the same `redirect_uri`
   and `code_verifier`, authenticating the client as above. Codes expire after two
   minutes and may be exchanged once.
4. Use the access token for the OneDev API. OIDC clients validate the ID token's
   signature using JWKS, issuer, audience, expiry and nonce. Its `sub` is the user's
   stable OneDev ID. Fetch requested profile/email claims from userinfo.
5. Refresh with `grant_type=refresh_token` and the current `refresh_token`.
   Persist the returned token pair atomically and serialize refresh requests.

Access tokens last at most one hour. Every explicit consent grants renewable
access for up to 30 days, including OAuth-only requests; the consent screen states
this duration. Refresh tokens rotate on every exchange. Refreshing replaces the
previous access token. Refresh requests may reduce scopes but cannot expand them;
removed role scopes also remove the corresponding project authorizations.
Refresh does not extend the original 30-day authorization.

Reusing an exchanged code or an old refresh token revokes the authorization and
its active token. Under **Authorized Applications**, each user sees one entry per
application. Opening the application shows all of that user's tokens, including
their scopes, project-role permissions, redirect URI, authorization date, last
token exchange, access-token expiry and authorization expiry. Token values and
refresh-token secrets are not displayed. Users can revoke a token separately or
revoke an application from its list row or use **Revoke All** on its details
page to revoke all their tokens for that application. **Revoke All** at the top
of the list revokes access for every application. Other users' authorizations
are unaffected.

Revocation immediately removes the authorization, its access tokens and its
project-role permissions. The application disappears from the user's list when
no authorizations remain; its registration is kept. There is no separate delete
action or persisted revoked/expired status. If the revocation transaction fails,
the authorization remains unchanged and the operation reports failure.
Expired authorizations and unused codes older than two minutes are rejected
and hidden immediately based on their expiration dates, then deleted by cleanup
daily at 00:00 in the server's time zone. Cleanup is rescheduled on startup;
records that expired during downtime are removed at the next scheduled run.
An expired access token with a valid refresh
authorization is retained so the client can refresh it; the whole authorization
is removed when its 30-day lifetime ends. Existing client application sessions
are not signed out by token revocation.

Application owners and administrators can revoke all users' authorizations from
the application's registration settings. Clients can revoke their own access or
refresh token through the revocation endpoint. Disabled users and disabled
personal application owners cannot use grants.

OIDC supports the authorization code flow, public subject identifiers, RS256,
query responses and explicit consent. `prompt=consent` is supported;
`prompt=none` returns `login_required` or `consent_required`. Request objects,
`request_uri`, `max_age`, `claims`, `acr_values`, `id_token_hint`, other prompt
values and alternative response modes are rejected. Signing keys are persisted
with server settings, survive restarts and are shared by cluster members.

## Client interoperability checks

The [Playwright test spec](../e2e-test/tests/oauth-provider.spec.js) uses
openid-client for OAuth2 and OIDC, with automated application registration and
browser consent. It covers public-client, client-secret Basic and client-secret
POST flows, including PKCE, project isolation, refresh, replay rejection and
revocation. OIDC checks verify ID-token signatures and claims. See the
[E2E README](../e2e-test/README.md#oauth2-and-openid-connect-provider) for setup
and the exact checks performed.

Git over HTTP is also supported through
[git-credential-oauth](https://github.com/hickford/git-credential-oauth). Register
a public client with redirect URI `http://127.0.0.1` (no trailing slash), then
configure the helper's `oauthClientId`, `oauthAuthURL` (`/~oauth/authorize`),
`oauthTokenURL` (`/~oauth/token`) and `oauthScopes` for your OneDev host. Request
the Code Reader role for clone/fetch or Code Writer for push, using the actual
role scope from discovery. Select the allowed projects during consent. Pair the
helper with a credential store that supports refresh tokens. The JavaScript spec
tests Git clone/fetch/push, permissions, refresh and revocation using issued
tokens directly; it does not test the helper executable or its credential cache.
