# OneDev end-to-end tests

This project uses [Playwright Test](https://playwright.dev/docs/intro) to test
the OneDev web interface in Chromium.

## Setup

Node.js 20 or newer is required.

```bash
cd e2e-test
npm install
npm run install:browsers
```

## Run the tests

```bash
npm test
```

By default Playwright starts `./dev.sh run` from the repository root and waits
up to ten minutes for `http://127.0.0.1:6610`. If a server is already listening
there, Playwright reuses it.

To test a server started separately, or a server at another URL, use:

```bash
E2E_SKIP_WEBSERVER=1 npm test
E2E_SKIP_WEBSERVER=1 E2E_BASE_URL=https://onedev.example.com npm test
```

For local debugging, `npm run test:headed` opens the browser and
`npm run test:ui` starts Playwright's interactive test runner.
