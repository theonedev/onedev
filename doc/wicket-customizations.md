# Why OneDev overrides Wicket 7.18.0

The files in `server-core/src/main/java/org/apache/wicket` shadow classes and
resources from the Wicket dependencies. Replacing them with a newer upstream
copy can silently remove application behavior even when compilation succeeds.

This audit compares all 22 tracked files in that directory with the **7.18.0
release sources**, the version declared by `wicket.version` in the root POM.
Formatting, import ordering, warning suppressions, wording changes, and removed
TODOs are not behavioral customizations. Historical changes already present in
7.18.0 (for example, form parameter selection by HTTP method) are not counted
as OneDev patches.

## Comparison baseline

The baseline is the Apache source jars published in
[Maven Central](https://repo.maven.apache.org/maven2/org/apache/wicket/).
For each artifact below, the source archive is
`<artifact>/7.18.0/<artifact>-7.18.0-sources.jar` relative to that URL.
These SHA-1 values identify the archives used for comparison:

| Artifact | Source jar SHA-1 |
| --- | --- |
| wicket-core | `79e376fd58bb30c064a9a96f18b9191b9e12dd80` |
| wicket-extensions | `c6af7ed32da7f2d8cabf0b69863568a2d360a515` |
| wicket-request | `d25defd74f28a7ebc156c82988aa1c8179226c1a` |
| wicket-util | `1fbe4064b18f75debb6b879b985eea1b961dff6d` |
| wicket-native-websocket-core | `07d34c7ae08f1cfe88d7e954e252be0d6f297e68` |
| wicket-guice | `2733ad290e2a48b761eba660dddcabdb8a118d05` |

All paths below are relative to `server-core/src/main/java/org/apache/wicket`.
Except for the JavaScript resource noted below, the archive entry has the same
`org/apache/wicket/...` path. To repeat an individual comparison, for example:

```sh
unzip -p ~/.m2/repository/org/apache/wicket/wicket-core/7.18.0/wicket-core-7.18.0-sources.jar \
  org/apache/wicket/markup/html/form/FormComponent.java > /tmp/FormComponent.upstream.java
diff -u /tmp/FormComponent.upstream.java \
  server-core/src/main/java/org/apache/wicket/markup/html/form/FormComponent.java
```

Reasons identified as **inferred** are supported by current callers or the
effect of the diff; available commit messages do not establish a more specific
original incident. Commit IDs below refer to this repository's history.

## Context for configurable editors

OneDev's `HierarchicalContext` lets code find the current editor, its enclosing
bean, project, and other component ancestors. In upstream Wicket these callback
boundaries do not establish this OneDev context.

| Override | Difference and motivation |
| --- | --- |
| `Component.java` | Pushes a `ComponentHierarchical(this)` context around initialization/re-addition, before-render, render (including after-render cleanup), and header rendering. Always pops it in `finally`. This makes contextual choices, defaults, and visibility logic resolve against the component being processed and prevents a child callback from leaving its context active for a sibling. |
| `RequestListenerInterface.java` | Applies the same scope to both component and behavior callbacks, including their authorization checks. Click/Ajax processing needs the originating editor's context just as rendering does; cleanup must also happen on rejection or failure. |
| `extensions/ajax/markup/html/autocomplete/AutoCompleteBehavior.java` | Establishes the owning component's context while the separately scheduled response obtains and renders suggestions. The listener's context has already ended by this stage. |

The editor rationale is inferred from `HierarchicalContext`, `ComponentHierarchical`,
and `web/editable/BeanEditor#getInputValue` and visibility evaluation. Commit
`cfc94c05d0` records a context refactoring, not the original reason for every hook.
`ComponentContextCustomizationTest` covers initialization/ancestor lookup,
listener failure, behavior authorization, and restoration of an enclosing context.
Before-render, render/head, and autocomplete response integration remain documented
contracts rather than separately exercised by these tests.

## Form values and validation

`markup/html/form/FormComponent.java` changes three related contracts:

* `convertedInput` starts with the supplied model value. Upstream leaves it null
  until conversion occurs.
* It is no longer transient or cleared during detach, so an uncommitted converted
  value survives subsequent requests and page serialization independently of the
  model value.
* `isValid()` visits ordinary child components as well as form components.
  Feedback errors placed on a label/container inside a composite editor therefore
  invalidate the editor. Warnings still do not invalidate it.

The retained-value motivation is inferred from dependent editor logic such as
`BeanEditor#getInputValue` and list editors reading child `getConvertedInput()`
values before the whole form is committed. Initializing once does not imply that
every later model change automatically synchronizes converted input. The broader
validation reason is explicitly stated in the source comment about errors on
ordinary components inside `FormComponentPanel`.

`FormComponentCustomizationTest` protects initial values, detach/serialization
without committing the model, and nested feedback validity.

## Dynamic classes and dependency injection

| Override | Difference and motivation |
| --- | --- |
| `application/AbstractClassResolver.java` | Resolves both generated issue-field bean names through `FieldUtils.getFieldBeanClass`, and generated parameter beans through `ParamUtils.loadBeanClass`, before consulting Wicket's normal weak class cache/classloaders. These classes come from runtime configuration rather than fixed classpath entries. Inferred motivation: restore generated editor state and select the current generated class instead of a cached obsolete definition. Ordinary classes still use upstream resolution. |
| `guice/GuiceProxyTargetLocator.java` | Obtains the injector from `AppLoader.injector` instead of `Application.get().getMetaData(GuiceInjectorHolder.INJECTOR_KEY)`. Inferred motivation: lazy injected proxies can resolve OneDev services when no Wicket application is bound to the thread. `WebApplication` also installs its component injector with this same application-wide injector. Added in `59315b3f81`, whose broad “Usability improvements” message does not establish a specific failing background operation. |

These contracts need OneDev's generated configuration/injector lifecycle to test
end to end; they are documented here rather than covered by mocks that merely
repeat the added calls.

## Page storage and request performance

| Override | Difference and motivation |
| --- | --- |
| `page/RequestAdapter.java` | Commits all touched pages without calling `isPageStateless()`. The source explicitly explains that inspecting a partially initialized page containing `wicket:enclosure` caused errors when anonymous access redirected to sign-in. The tradeoff is storing touched pages that upstream would filter out as stateless. `RequestAdapterCustomizationTest` verifies storage without invoking that unsafe inspection. |
| `page/PageStoreManager.java` | Removes the session-local page list and its custom serialization/deserialization. `SessionEntry.getPage()` goes directly to `IPageStore`; `setSessionCache()` remains as a no-op. Inferred motivation: avoid retaining/serializing a second copy of page state in the HTTP session and let the page store own it. This older change is already present at the earliest available import; a specific clustering/performance incident is not established. Separately, touched-page storage suppresses only `IllegalStateException` messages containing `Response is committed`, logging at debug. Commits `740f4ba5be` and `70052bc21c` document the case of trying to create a session after response commitment. Other such exceptions still propagate. |
| `request/cycle/RequestCycle.java` | Caches rendered resource and bookmarkable-page URLs in each request cycle, keyed by reference/page class and parameters. Commit `017e7e7e8d` ties caching to page-loading performance (#429); `5f3d6616a4` records support without a session. The current implementation is request-scoped even when a session exists. Also removes the extra development-mode exception banner and logs nested Jetty `EofException` at debug when error-response processing itself fails, reducing duplicate/disconnect noise. |

`RequestCycleCustomizationTest` checks cache reuse, different parameters/resources,
and a fresh cache in another request. The cache retains the supplied mutable
`PageParameters`, so these tests do not promise correctness if a caller mutates
parameters or changes the URL renderer's base after caching a URL. Page-store
lifecycle and exception logging are documented, not covered by these tests.

## URLs, login, and browser behavior

| Override | Difference and motivation |
| --- | --- |
| `core/request/mapper/BasicResourceReferenceMapper.java` | Emits the resource scope class name in lowercase and remembers the original name in a static reverse map. Decoding uses that map, falling back to the supplied name. The explicit reason in both code and `d642b2383f` is that some agents lowercase URLs. This only normalizes the class-name segment; it does not make every resource path case-insensitive. Mapping depends on the URL having been generated in the process, and lowercasing uses the default locale. |
| `request/http/handler/RedirectRequestHandler.java` | Sends every target through `UrlRenderer.renderRelativeUrl(Url.parse(...))`, replacing upstream's special handling for leading `/`. Inferred motivation: render redirects relative to the current request, avoiding dependence on servlet-container conversion to an absolute backend URL. The original incident is not established by the available history. |
| `RestartResponseAtInterceptPageException.java` | Clears host, port, and protocol on the saved original URL, so login does not retain the backend origin exposed by a reverse proxy (#137, `5621ed17eb`). Before redirecting back, removes query parameter names containing `.IBehaviorListener.` so an expired Ajax callback does not become the post-login destination (#1301, `ce535c2842`). Other destination parameters are retained. |
| `protocol/http/servlet/ServletWebResponse.java` | For normal non-Ajax browser redirects, writes a script that saves the current fragment in `sessionStorage`, restores it for the matching destination when that destination has no explicit fragment, and navigates with `location.replace`. URL fragments never reach the server; this preserves notification links to comments across login (#114, `55ac3833c6` and `4138bd7cb5`). Agents matching bot/crawler/spider/crawling still receive a servlet redirect for SEO (`41d49e8734`); Ajax retains upstream's Ajax redirect response. The proxy fix also changes how fragment storage keys are formed. |

Browser verification when changing this group: follow a comment notification
while logged out, sign in behind a proxy/context path, and check both the returned
page and anchor. Also check an expired Ajax interaction, an explicit destination
fragment, and a crawler request. The new Java unit tests do not execute these
browser flows.

## WebSocket integration

| Override | Difference and motivation |
| --- | --- |
| `protocol/ws/api/ServletRequestCopy.java` | Does not call `getCharacterEncoding()`, `getContentType()`, or `getRequestedSessionId()` on the upgrade request; saves null for each. The source comment records `UnsupportedOperationException` during connection. It also implements newer Servlet API methods (`getContentLengthLong`, `changeSessionId`, `upgrade`) with placeholder return values so the copied request satisfies the interface. Those placeholders are not a real session-rotation/upgrade implementation. |
| `protocol/ws/api/AbstractWebSocketProcessor.java` | Calls OneDev's `WebSocketService.onConnect` after the Wicket connected event. `DefaultWebSocketService` explicitly explains that this replays recent observable changes missed between page rendering and connection establishment. Processing exceptions send `WebSocketMessages.ERROR_MESSAGE` to the client in addition to logging. Exposes `getRegistryKey()` to the OneDev processor to construct its `PageKey`. Removes Wicket's `onError`/`WebSocketErrorPayload` broadcast path; the OneDev Jetty processor handles transport errors itself. These changes integrate the application notification/error lifecycle; a separate original incident for each is not established. |
| `protocol/ws/api/WebSocketRequestHandler.java` | Includes session feedback in partial updates, triggers OneDev's `beforeElementReplace`/`afterElementReplace` events for replaced elements, and re-registers page observables after component replacements. This keeps client widgets and observation subscriptions consistent with the newly rendered component tree (`91f6c8dbfe`, and current `WebSocketService.observe` usage). It assumes the page is a OneDev `BasePage`. |
| `protocol/ws/api/WicketWebSocketJQueryResourceReference.java` | Selects the adjacent customized JavaScript resource instead of upstream's `res/js/wicket-websocket-jquery.js`. It must be considered together with the following file. |
| `protocol/ws/api/wicket-websocket-jquery.js` | Compared against `org/apache/wicket/protocol/ws/api/res/js/wicket-websocket-jquery.js` in the source jar. Defers an XML Ajax response while `onedev.server.ajaxRequests.count > 0`, retrying after 100 ms before entering Wicket's message channel. Commit `49e16799e2` fixes the revision-indexing message remaining visible after CI/CD spec editing (OD-2661): an in-flight Ajax update could otherwise race the pushed DOM update. Non-XML messages retain upstream publication behavior. |
| `protocol/ws/api/registry/SimpleWebSocketConnectionRegistry.java` | **No behavioral customization relative to 7.18.0**; the only difference is an extra blank line. It is a candidate for removal in a separate cleanup, not a contract that needs a regression test. |

WebSocket tests should eventually exercise the Jetty upgrade request, changes
arriving before connection, DOM updates overlapping Ajax, feedback rendering, and
subscription changes after replacement. None of these integration flows is claimed
as covered by the Java tests added for this audit. Current keep-alive handling in
OneDev's `WebSocketProcessor` is outside the upstream diff; the historical
keep-alive branch removed by `99f08a7edb` is not a current customization here.

## Navigation and diagnostics

| Override | Difference and motivation |
| --- | --- |
| `extensions/markup/html/repeater/data/table/NavigationToolbar.java` | Recreates the paging navigator before each render rather than once in the constructor, and omits the upstream navigator label. Inferred reason for recreation: use current table state when constructing OneDev's navigator. Label removal is recorded in `dd1599c6a5` (OD-1965); `DefaultDataTable` now chooses a separate `ListNavigationToolbar` when a count is requested. |
| `extensions/markup/html/repeater/data/table/NavigationToolbar.html` | Removes the separate label and renders the navigator as a `ul`, matching `OnePagingNavigator` and OneDev's pagination styling. Keep markup and Java overrides together. Validate by changing result/page counts and checking pagination with and without the count toolbar. |
| `util/string/Strings.java` | The substantial text diff is mostly indentation. The only runtime change is `outputThrowable` using `cause.getMessage()` instead of `cause.toString()`. The source explicitly says this avoids including the whole HTML template in diagnostics. Messages and stack frames remain; exception-class text from `toString()` is also omitted. `StringsCustomizationTest` covers a nested exception with template-producing `toString()`. |

## Running the focused regression tests

```sh
./dev.sh test -pl server-core -Dtest='*CustomizationTest'
```

Validation at the time of this audit: all 10 tests passed with OneDev classes.
Running the same tests with the original `wicket-core` and `wicket-util` 7.18.0
binary jars ahead of OneDev classes on the test classpath produced 10 expected
behavioral failures. No production source was changed for that comparison.

The local managed JVM blocked Mockito's automatic agent attachment. The successful
Maven run supplied the cached Byte Buddy agent explicitly via
`-DargLine=-javaagent:/path/to/byte-buddy-agent.jar` (version 1.12.19 for that run);
no project build settings were changed.

The tests deliberately exercise behavior that differs from 7.18.0. They complement
the motivations and verification scenarios above; they are not a complete Wicket
compatibility suite. During an upgrade, compare against the new release, keep
each customization whose application contract still applies, and retire overrides
that have become equivalent to upstream.
