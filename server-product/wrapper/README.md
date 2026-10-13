# OneDev packaging integration

The wrapper source now lives in the separate
[wrapper project](https://code.onedev.io/onedev/wrapper).
This directory contains OneDev's JVM defaults, prepared distribution files, and
`update.sh` for downloading and importing releases and verifying packaging. The generic
wrapper project has no OneDev application code or packaging profile.
To update to the latest release from the
[public download site](https://code.onedev.io/onedev/wrapper/~sites/public/),
run from this repository (requires Bash, curl, tar, a SHA-256 utility, and network access):

```sh
./server-product/wrapper/update.sh
```

The command downloads the release archive and its SHA-256 checksum and imports
them using the same verification and adaptation described below. The site contains
the latest successful publication. Downloads are removed after the update.
Review and commit the updated files in this directory with the server changes.

For local testing, build in the sibling wrapper checkout and import its
output directly, without creating or downloading a release archive:

```sh
(cd ../wrapper && ./build.sh)
./server-product/wrapper/update.sh ../wrapper
```

Pass `/path/to/wrapper` for a checkout elsewhere. This copies the
available binaries and `wrapper.jar` from `dist/boot`, adapts the checkout's
launcher templates, and refreshes both server and agent files and checksums.
Platforms absent from the local build retain their bundled binaries, so a
host-only `./build.sh` is sufficient. All binaries already in `dist/boot` are
imported, including those from previous builds. `VERSION` is marked with a
`-local` suffix. Refresh the development sandbox as described below, or repackage
OneDev to update a release archive; `./dev.sh build` alone does not refresh wrapper
files in an existing sandbox. Local imports are for development;
run the updater without arguments to restore a published release before committing
distribution files.

To also refresh wrapper binaries and scripts in an existing development sandbox,
stop its server and add `--sandbox` to the update command:

```sh
./server-product/wrapper/update.sh --sandbox
# Or import a local build and then refresh the sandbox:
./server-product/wrapper/update.sh ../wrapper --sandbox
```

After successfully updating the prepared distribution, this copies the server and
embedded agent launchers, native executables, and `wrapper.jar` into
`server-product/target/sandbox`. It preserves configuration, application JARs,
logs, and site data. If the sandbox does not exist, the command prints an error
and exits before downloading or importing anything. Create it with `./dev.sh build`
first. The flag also works with `import`; when using `--destination`, the sandbox
is located at `../target/sandbox` relative to that destination. Distribution
archives still need to be repackaged separately.

To import an archive downloaded separately, place `wrapper-VERSION.tar.gz`
and its `.sha256` together, then run:

```sh
./server-product/wrapper/update.sh import /path/to/wrapper-VERSION.tar.gz
```

The importer verifies the archive and file checksums, copies all upstream boot
files, adapts generic launchers for server/agent and one-shot utilities, and creates
the legacy macOS executable alias. It renders server and agent configurations from
`wrapper.conf` and regenerates `SHA256SUMS`. These integration rules live in the
server project. The agent configuration retains the `agentVersion` placeholder,
which is resolved when assembling a standalone agent or downloading one.

`distribution/` contains the exact installation-relative files Maven will copy:

- `boot/`, `bin/`, and `conf/` contain the server's wrapper files.
- `agent/boot/`, `agent/bin/`, and `agent/conf/` contain the agent's wrapper files.

The service names are `onedev.server` and `onedev.agent` on all platforms.
Windows uses these SCM names, Linux uses `onedev.server.service` and
`onedev.agent.service` (or the same names without `.service` for SysV), and
macOS uses these names as launchd labels and plist filenames.

Keep this prepared tree in version control. The maintenance script uses Bash 3.2+
and standard Unix tools (`sha256sum` on Linux or `shasum` on macOS); no Python or
Node.js runtime is needed. Ordinary Maven builds consume the checked-in files. Importing recreates
the tree, so removed upstream files cannot survive into a new release. To change
JVM defaults or the adaptation rules, edit `wrapper.conf` or the importer and
reimport the pinned release archive.

The Maven plugin has no wrapper Maven dependency, does not download its
releases, and does not require its source checkout. It copies these prepared files
into the server and agent distributions. Rust is not needed to package OneDev.

The companion maven-plugin 2.9.1 consumes a generic prepared distribution instead
of interpreting JSW/native layouts, launcher templates, platform names, or wrapper
configuration keys. Publish that plugin change before the server change.
`server-product/pom.xml` configures the prepared directory, generated paths to
replace on incremental builds, agent files needing version substitution, and
executable permissions. Future wrapper changes belong in this repository's
importer, inputs, or POM and do not require a Maven plugin release as long as this
prepared-file contract remains the same.

Validate the resulting release with (the package checker requires `unzip`, and
the tests also use `zip` to create package fixtures):

```sh
./server-product/wrapper/update.sh test
./server-product/wrapper/update.sh check-package server-product/target/onedev-VERSION.zip
```

Run `./server-product/wrapper/update.sh help` for all commands and import options.
Running the script with no arguments is equivalent to the `update` command.

Existing `server.sh` / `agent.sh` commands and `conf/wrapper.conf` remain supported.
The Java lifecycle API is `io.onedev.wrapper.WrapperManager`, with
`restartAndReturn()` and `stopAndReturn(int)`. The launcher class is
`io.onedev.wrapper.WrapperSimpleApp`. Update the wrapper and application jars
together when migrating from an earlier Java API package.
Stop and remove an existing service with its old launcher and configuration before
replacing wrapper files or changing service names. Set `wrapper.name` to
`onedev.server` or `onedev.agent`, and `wrapper.java.mainclass` to
`io.onedev.wrapper.WrapperSimpleApp`, then install and start the service again.
Server upgrades replace boot files and scripts while retaining `wrapper.conf`
and updating its lifecycle bridge class.
Existing agents update only application jars: migrating their wrapper requires
stopping them and replacing `boot` and `bin` from a new agent distribution while
retaining configuration. Newly downloaded agents use Wrapper.

Review the standalone project's configuration compatibility list before migrating
custom JSW features. Native SCM, launchd, and systemd acceptance testing is still
required before the initial production release.

The Docker upgrade invocation explicitly disables crash retries for its one-shot
operation. Generic Wrapper does not inspect the application's class name.
Launchers supply their own paths when installing services. When invoking a native
binary directly to install a service, specify `wrapper.script=../bin/server.sh`
or `wrapper.script=../bin/agent.sh` for the corresponding distribution.
