# Project Instructions

Do not add `Co-authored-by:` or other co-author / AI attribution footers to
commit messages unless the user explicitly asks for them. If such a trailer is
injected into a commit you just created (and it is not pushed), rewrite the
commit message to remove it before finishing.

Never modify generated translation files, including any `Translation_*.java`
files and files under
`server-core/src/main/java/io/onedev/server/web/translation/`. Translation
files are generated automatically.

For local development, start the OneDev server with `./dev.sh run` from the
repository root. The server can hot-load changed classes. Use `./dev.sh build`
to compile them; the first run performs a full compile, while subsequent runs
compile only the changes. Watch the server console for reload errors and
restart the server if one occurs.
