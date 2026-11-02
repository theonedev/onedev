# Project Instructions

Do not add `Co-authored-by:` or other co-author / AI attribution footers to
commit messages unless the user explicitly asks for them. If such a trailer is
injected into a commit you just created (and it is not pushed), rewrite the
commit message to remove it before finishing.

Never modify generated translation files, including any `Translation_*.java`
files and files under
`server-core/src/main/java/io/onedev/server/web/translation/`. Translation
files are generated automatically.

Do not add tests for trivial or minor changes unless the user explicitly asks
for them.

Do not run Maven after changing files unless the user explicitly asks for it.

Prefer field injection over constructor injection wherever possible for
`@Singleton` services and Wicket components/pages.

**Service Permission Contract:** Public service methods that check a user's
permissions must accept the user's `Subject` instead of a `User` for those
checks. A `Subject` parameter signals permission checks; without one, the
method runs without permission checks. Internal methods may accept `User`
and leave authorization to their callers. Document any exception explicitly
on the method.

**Open Session in View:** A single Wicket request cycle is normally handled
within a single Hibernate session. OneDev opens the session at the start of
the request cycle and closes it afterwards.

**Hibernate Session Isolation:** Do not share Hibernate-loaded entity objects
across Hibernate sessions. For example, a project loaded by ID during Wicket
page initialization must not be reused in a link's `onClick` listener, which
runs in another request cycle and Hibernate session. Store the project ID
instead, and load the project again using that ID in the listener's session.

**Wicket Serialization Safety:** Wicket pages, behaviors, and components are
serialized to disk after a request is processed. Their serialized state must
not contain any Hibernate entities. Store Hibernate entities either in
`transient` fields or in detachable Wicket models, and detach those models
in the `onDetach()` method of the relevant page, behavior, or component.

Use the bean editor for settings UI whenever possible. Style UI appropriately
to look polished and consistent with other pages. Prefer existing CSS classes
from Bootstrap or `base.css` whenever possible.

Users may continue privileged work in an existing session after their
privileges are revoked. Revoked privileges must be denied in a new session.
Do not report continued access within the existing session as a security bug
or require immediate revocation within that session.

Run `./dev.sh prod` from repository root to start dev server if you want to 
do some tasks with the live instance. Make sure to stop it by running 
`./dev.sh stop` after tasks are finished.
