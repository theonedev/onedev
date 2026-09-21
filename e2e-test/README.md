# OneDev end-to-end tests

This project uses [Playwright Test](https://playwright.dev/docs/intro) to test
the OneDev web interface in Chromium.

## Setup

Node.js 20 or newer is required. The service desk tests also require Java and
Maven to install and run the local GreenMail server.

```bash
cd e2e-test
npm install
npm run install:browsers
npm run install:mail-server
```

## Run the tests

```bash
npm test
```

By default Playwright starts `./dev.sh run` from the repository root and waits
up to ten minutes for `http://127.0.0.1:6610`. If a server is already listening
there, Playwright reuses it.

Before running tests, each worker polls `~api/server/ready` without
authentication until it returns `true`, allowing up to ten minutes for server
initialization to finish.

When Playwright starts the server, it passes OneDev
[unattended setup](https://docs.onedev.io/installation-guide/run-as-docker-container)
environment variables so the first-run wizard is skipped: admin user/password
`admin`/`admin`, email `admin@example.com`, and `initial_server_url` set to
`http://<host-ip>:6610` (SSH root URL left unset). These only apply on a fresh
data directory.

To test a server started separately, or a server at another URL, use:

```bash
E2E_SKIP_WEBSERVER=1 npm test
E2E_SKIP_WEBSERVER=1 E2E_BASE_URL=https://onedev.example.com npm test
```

For local debugging, `npm run test:headed` opens the browser and
`npm run test:ui` starts Playwright's interactive test runner.

## Application-relative redirects

`tests/redirect-request-handler.spec.js` exercises `RedirectRequestHandler` via
the project site's missing-trailing-slash redirect. It verifies that the project
path is not duplicated when resolving the application-relative destination,
for both normal browsers (JavaScript redirect) and crawlers (HTTP redirect).
The fixture's empty site must display its directory listing at the canonical
URL, rather than an unrelated project-not-found page.

## Login redirects and page-store warnings

`tests/login-redirect.spec.js` checks protected system settings and private
projects with fresh and existing anonymous sessions. It verifies session
replacement, return to the requested URL with its query parameters, recovery
from an incorrect password, and authenticated access after reload. A second-tab
login must close the previous session's WebSocket; replaying the old session
cookie after login must still require authentication.

It also opens a private issue comment link while logged out, verifies the
fragment is saved during login and restored afterward, and checks that the
comment is scrolled into view. The saved fragment is consumed, so a later visit
without a fragment does not jump back to the comment.

Wicket catches exceptions from its page-store statelessness check, so the
browser can complete a redirect while the server logs an uninitialized
`wicket:enclosure` error. To include this regression check, add the following
logger to the test server's `conf/logback.xml` and restart it:

```xml
<logger name="org.apache.wicket.pageStore.RequestPageStore" level="WARN"/>
```

The default `org.apache.wicket` logger is set to `ERROR`, which hides these
warnings. Pass the server's local log file when running the test:

```bash
E2E_SKIP_WEBSERVER=1 \
E2E_SERVER_LOG=../server-product/target/sandbox/logs/server.log \
npm test -- tests/login-redirect.spec.js
```

The test attaches the log entries written during each case and fails on
statelessness-check or enclosure errors. Use a dedicated test server and a log
file that will not rotate during the run. Without `E2E_SERVER_LOG`, only the
browser assertions run, which also supports remote test servers.

## WebSocket partial updates

`tests/websocket-request-handler.spec.js` covers the OneDev customizations in
Wicket's `WebSocketRequestHandler` through real browser WebSocket connections:

- An issue title change made through REST replaces its list row, with
  `beforeElementReplace` seeing the attached old node and `afterElementReplace`
  seeing the attached replacement.
- A link added after the issue page loads introduces a new state badge through
  a WebSocket update. Changing only the linked issue then updates that badge
  twice on the same connection, exercising subscription registration after rendering.
- Submitting the iteration form in the same browser session, without following
  its redirect, leaves a pending success message. An independent issue change
  delivers that message in the WebSocket response and displays the feedback panel.

`tests/websocket-resource-reference.spec.js` checks that
`WicketWebSocketJQueryResourceReference` selects OneDev's adjacent JavaScript
and that its Ajax coordination works. It holds a real issue-query Ajax response,
changes the issue through REST, and waits for the resulting WebSocket frame.
The DOM must retain the old title while Ajax is pending, then show the newer
pushed title after the held response completes. This protects against an older
Ajax response overwriting a newer WebSocket update (OD-2661).

The tests create and clean up their own projects and use the default `Related`
issue link and `Open`/`Closed` states. They do not require changes to server code
or logging configuration. For an existing server, set `E2E_ADMIN_USER` and
`E2E_ADMIN_PASSWORD` if its credentials differ from `admin`/`admin`.

```bash
npm test -- tests/websocket-*.spec.js
```

## Jev issue suggestions

`tests/jev-issue-suggestions.spec.js` configures a fake API key and tests the real
new-issue form against a local HTTP mock. It verifies the outgoing authorization,
model, title, description and choice questions; saving suggested values;
ignoring low-confidence/unknown choices; preserving a manual choice; and
discarding a response after the title changes.

Jev requests originate in the Java server, so browser request interception cannot
mock them. The fixture starts a local HTTP server on an automatically assigned
port, then saves its URL and a fake API key through the Jev settings page. It
restores the original settings afterward. Jev's normal base URL defaults to
`https://api.typesafe.ai/v1`; custom URLs can include a port, and `/systemone` is
appended automatically.

```bash
npm test -- tests/jev-issue-suggestions.spec.js
```

These tests also work with an already-running local OneDev server:

```bash
E2E_SKIP_WEBSERVER=1 npm test -- tests/jev-issue-suggestions.spec.js
```

Use one worker and a dedicated test server with the default Type and Priority
fields: the fixture temporarily removes their defaults and changes Jev settings,
restoring both after each case. No special server startup options are needed.

The mock listens on the test runner's loopback interface, so run the Java server
on the same host. No real Jev key or external API calls are needed.

## Jev service desk fields

`tests/jev-service-desk.spec.js` sends a generated email over SMTP to a local
[GreenMail](https://greenmail-mail-test.github.io/greenmail/) mailbox. OneDev's
real SMTP/IMAP connector polls that mailbox and creates the issue through the
service desk. The tests reuse the Jev mock in `tests/jev-fixtures.js`.

Install the pinned standalone mail server once (requires Maven and Java), then
run both Jev suites:

```bash
npm run install:mail-server
npm test -- tests/jev-*.spec.js
```

The mail fixture starts and stops GreenMail automatically on local ports. It
waits for OneDev to poll the empty inbox before sending, because OneDev skips
pre-existing messages on its first connection. Outgoing notifications also stay
inside GreenMail. Its protocol log is attached to each test result.

The tests verify created issue fields and configured fallbacks for low-confidence
or unknown answers and API failures.

Use a dedicated server and one worker, as with the new-issue form tests. Test
settings are restored, and test projects and users are removed. No external
mailbox, SMTP credentials or real Jev key is
required. Both Jev suites run as part of the normal test run.
