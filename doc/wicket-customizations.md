# Why OneDev overrides Wicket 10.11.0

The 17 Java classes and two resources under
`server-core/src/main/java/org/apache/wicket` shadow the Wicket dependencies.
They were compared with the 10.11.0 release source jars, and their application
patches were checked against the former 7.18.0 overrides and Git history.
Formatting, import ordering, and warning suppressions are not customizations.

Baselines are the `wicket-core`, `wicket-extensions`, `wicket-request`,
`wicket-util`, `wicket-native-websocket-core`, and `wicket-guice` source jars in
[Maven Central](https://repo.maven.apache.org/maven2/org/apache/wicket/), at
`<artifact>/10.11.0/<artifact>-10.11.0-sources.jar`.
Paths below are relative to `server-core/src/main/java/org/apache/wicket`.

## Component context and forms

| Override | Retained behavior |
| --- | --- |
| `Component.java` | Push/pop OneDev's `HierarchicalContext` around initialization/re-addition, before-render, render/after-render, and header rendering. Contextual choices, defaults, and visibility resolve against the current editor, with cleanup on exceptions. |
| `core/request/handler/ListenerRequestHandler.java` | Scope both component and behavior callbacks, including authorization, to the originating component. This replaces the removed Wicket 7 `RequestListenerInterface` override. |
| `extensions/ajax/markup/html/autocomplete/AutoCompleteBehavior.java` | Scope the separately scheduled suggestions response to its owning component. The listener's context has already ended at that point. |
| `markup/html/form/FormComponent.java` | Initialize converted input from the model, retain it through detach and serialization, and include errors on ordinary child components when determining validity. Dependent editors can read uncommitted values, and composite editors fail validation when their labels/containers have errors. |

`ComponentContextCustomizationTest` covers initialization/ancestor lookup,
listener failure, authorization, and restoration of enclosing context.
`FormComponentCustomizationTest` covers initial and uncommitted values,
serialization, and nested feedback. Rendering/head and autocomplete callback
scopes remain source-audited contracts, rather than separate integration tests.

## Dynamic classes and dependency injection

| Override | Retained behavior |
| --- | --- |
| `application/AbstractClassResolver.java` | Resolve generated issue-field and parameter beans through `FieldUtils`/`ParamUtils` before Wicket's weak class cache. Generated classes follow the current configuration instead of an obsolete cached definition. |
| `guice/GuiceProxyTargetLocator.java` | Resolve lazy injections using `AppLoader.injector`, including when no Wicket application is bound to the thread. |

These behaviors still differ from Wicket 10 and depend on OneDev's runtime class
and injector lifecycle. The rationale is inferred from the callers; the available
history does not establish a separate original incident for each hook.

## Page storage and request processing

| Override | Retained behavior |
| --- | --- |
| `pageStore/RequestPageStore.java` | Store touched pages without inspecting `isPageStateless()`, which can evaluate an uninitialized enclosure during a login redirect. Suppress only `IllegalStateException` messages containing `Response is committed` during session binding/page storage, with debug logging. Clear pending pages after this failure so detach cannot retry session creation. Other failures propagate. These behaviors replace the former `RequestAdapter` and part of `PageStoreManager`. |
| `request/cycle/RequestCycle.java` | Cache rendered resource and bookmarkable-page URLs within each request (#429), suppress the extra development-mode exception banner, and log nested Jetty EOF failures at debug when error-response handling fails. |

The old `PageStoreManager` session-cache customization is implemented through
OneDev's `web/page/store/PageManagerProvider`: it omits Wicket's session cache of
live pages and writes serialized pages synchronously to disk, loading them on
demand. No serialized-page cache or asynchronous write queue is retained in heap;
page objects and serialization buffers still exist while processing requests.
Requests wait for disk writes to complete. This uses Wicket 10's store extension
points instead of copying its old page manager.
`RequestAdapterCustomizationTest` covers both lifecycle phases,
committed responses, unexpected exceptions (including null messages), and avoiding
statelessness checks.

The Chromium comparison in `e2e-test/tests/login-redirect.spec.js` also reproduces
the enclosure problem with real anonymous requests to system settings. Restoring
Wicket 10.11.0's original `isPageStateless` helper produces
`Could not find child with id: ingressUrl in the wicket:enclosure` in both `end()`
and `detach()`. Wicket catches these exceptions and assumes the page is stateful,
so login can still complete. OneDev's default Wicket log level (`ERROR`) hides
the warnings. The override prevents the exceptions; the optional
`E2E_SERVER_LOG` assertion detects them with the page-store logger at `WARN`.
See `e2e-test/README.md` for the command and logging setup.

`RequestCycleCustomizationTest` covers cache reuse and request/parameter isolation.
The URL caches retain mutable `PageParameters`; their existing contract does not
promise correctness if callers mutate parameters or the renderer's base URL after
caching. Exception logging is source-audited, not asserted by those tests.

## URLs and login

| Override | Retained behavior |
| --- | --- |
| `core/request/mapper/BasicResourceReferenceMapper.java` | Lowercase the scope class segment for agents that lowercase URLs, and maintain the reverse class-name map (`d642b2383f`). The port uses `Locale.ROOT`. The map still depends on the URL having been generated in the process. |
| `request/http/handler/RedirectRequestHandler.java` | Rebase application-relative redirect destinations against the current request with `UrlRenderer.renderRelativeUrl` (`59b517746c`). |
| `RestartResponseAtInterceptPageException.java` | Remove the backend host/port/protocol from the saved URL (#137), and remove behavior callback metadata before returning from login (#1301). Wicket 10 encodes numeric behavior IDs, so the patch now uses `MapperUtils.parsePageComponentInfoParameter` instead of searching for `.IBehaviorListener.`. Page-version and ordinary valued query parameters survive. |
| `protocol/http/servlet/ServletWebResponse.java` | Preserve browser fragments across login using session storage and a scripted redirect (#114). Crawlers retain servlet redirects; Ajax retains upstream Ajax redirects. |

`RedirectRequestHandler` was introduced in `59b517746c` ("Fix the issue that page
redirecting works abnormally"). Wicket 10.11.0 leaves destinations without a
leading slash unchanged. For example, `ProjectSiteFileResource` redirects
`project/~site` to the application-relative `project/~site/`. Passing that string
directly to the browser resolves it as `project/project/~site/`; rendering it
relative to the current request produces the intended `project/~site/` URL.
`e2e-test/tests/redirect-request-handler.spec.js` verifies this using a newly
created project's empty site, including both scripted browser redirects
and crawler HTTP redirects. The destination's directory listing also confirms
that the redirect reaches the correct resource. Replacing only the URL-rendering
customization with Wicket 10.11.0's implementation makes both cases fail with a
duplicated project path; restoring the customization makes both pass.

`InterceptRedirectCustomizationTest` generates callback metadata using Wicket 10's
encoder and verifies removal, ordinary/duplicate parameters, page versions, and
backend-origin removal. It reproduces the old string-matching patch's failure.
`e2e-test/tests/login-redirect.spec.js` verifies a logged-out private issue
comment link through login, including fragment storage/restoration, scrolling
to the comment, and consuming the saved fragment. Proxy/context paths, explicit
destination fragments, and crawler/Ajax requests still need verification when
the redirect script changes.

OneDev's `LoginPage` calls
`RestartResponseAtInterceptPageException.replaceSessionPreservingOriginalDestination()`.
This helper saves only the intercepted URL and POST parameters, calls the normal
`WebSession.replaceSession()`, and restores that one metadata entry. Replacing
the session destroys the old HTTP session, runs WebSocket/chat cleanup listeners,
and clears OneDev's session fields and unrelated metadata. Plain ID rotation
would bypass this cleanup. Wicket 10's unwrapped `replaceSession()` loses the
intercepted destination and returns users to the home page after login.

`InterceptRedirectCustomizationTest` verifies the preserved request and removal
of unrelated metadata, servlet attributes, and OneDev session fields; ordinary
session replacement still clears the destination. The browser tests cover
fresh/existing anonymous sessions, a rejected password followed by successful
login, page reloads, closure of an old tab's WebSocket, and rejection of the old
session cookie after login.

## WebSockets

| Override | Retained behavior |
| --- | --- |
| `protocol/ws/api/AbstractWebSocketProcessor.java` | Replay observable changes missed between rendering and connection through `WebSocketService.onConnect`, and send OneDev's error message when processing fails. Wicket 10 already exposes `getRegistryKey()` to the subclass. Its transport error API can remain: OneDev's Jetty listener handles transport errors directly without calling that broadcast path. |
| `protocol/ws/api/WebSocketRequestHandler.java` | Include session feedback, emit `beforeElementReplace`/`afterElementReplace`, and re-register observables after replacing components. The customization now extends `XmlPartialPageUpdate`; it still assumes a OneDev `BasePage`. |
| `protocol/ws/api/WicketWebSocketJQueryResourceReference.java` | Select the adjacent customized JavaScript instead of the upstream `res/js/` path. |
| `protocol/ws/api/wicket-websocket-jquery.js` | Defer pushed XML Ajax responses while a OneDev Ajax request is in flight (OD-2661), preserving Wicket 10's channel handling and non-XML messages. |

`e2e-test/tests/websocket-request-handler.spec.js` exercises all three
`WebSocketRequestHandler` patches with real browser connections: replacement
events surround an issue row's DOM change, a newly rendered linked-issue badge
receives subsequent state changes, and pending session feedback appears in a
pushed update. REST changes trigger the pushes without an Ajax request or page
reload in the receiving tab.

`e2e-test/tests/websocket-resource-reference.spec.js` checks the selected
JavaScript resource and its Ajax coordination: a received WebSocket update
waits until a held issue-query response completes, then applies the newer
title. Both responses come from the server; the test controls their delivery
order to reproduce the OD-2661 race.

The `ServletRequestCopy` override was removed. `WebSocketProcessor` now passes
Jetty 12's real `HttpServletRequest` to Wicket; the old Jetty adapter that threw
on character encoding, content type, and requested session ID is gone. Wicket 10's
copy implements Jakarta Servlet. `JakartaJettyIntegrationTest` verifies those
metadata getters with a real Jetty request. It does not claim to test the entire
browser WebSocket reconnect/DOM lifecycle.

`protocol/ws/api/registry/SimpleWebSocketConnectionRegistry.java` was removed:
it was identical to Wicket 10.11.0 apart from whitespace, and had no behavioral
patch relative to 7.18.0 either.

## Navigation and diagnostics

| Override | Retained behavior |
| --- | --- |
| `extensions/markup/html/repeater/data/table/NavigationToolbar.java` | Recreate the navigator before rendering and omit the separate count label (OD-1965). OneDev selects a different toolbar when a count is wanted. |
| `extensions/markup/html/repeater/data/table/NavigationToolbar.html` | Render the navigator as a `ul`, matching `OnePagingNavigator` and OneDev's pagination styling. |
| `util/string/Strings.java` | Use `cause.getMessage()` instead of `cause.toString()` when printing exceptions, avoiding whole HTML templates in diagnostics. Stack frames remain. |

`StringsCustomizationTest` covers nested exceptions with template-producing
`toString()`. Pagination state changes remain a UI verification scenario.

## Verification

```sh
./dev.sh test -pl server-core '-Dtest=*CustomizationTest,JakartaJettyIntegrationTest'
```

On JVMs that disallow Mockito self-attachment, set `JAVA_TOOL_OPTIONS` to
`-javaagent:/absolute/path/to/byte-buddy-agent.jar` for the command. The local audit
used the cached 1.12.19 agent. See [the library audit](library-customizations.md)
for the other libraries and retired overrides.
