#!/bin/bash
# Maintain OneDev's bundled Wrapper release (compatible with Bash 3.2).
set -euo pipefail
export LC_ALL=C
WRAPPER_DIR=$(cd "$(dirname "$0")" && pwd)
SCRIPT="$WRAPPER_DIR/$(basename "$0")"

usage() {
	cat <<EOF
Usage: $0 [COMMAND]
       $0 [WRAPPER_DIRECTORY] [--sandbox]

Commands:
  update [DIRECTORY] [--sandbox] Download and import the latest release (default)
                                 or copy build.sh output from the specified checkout
  import ARCHIVE [OPTIONS]       Import a local release archive
    --checksum FILE              Use this checksum file instead of ARCHIVE.sha256
    --destination DIRECTORY      Import into this directory instead of the script directory
    --sandbox                    After updating, refresh wrapper binaries and scripts
                                 in ../target/sandbox relative to the import directory
  check-package SERVER.zip [DIR] Verify server and agent packaging against DIR
                                 (defaults to the script directory)
  test                           Run offline importer, updater, and package checks
  help                           Show this help
EOF
}

fail() {
	echo "$*" >&2
	exit 1
}

require_sandbox() {
	[ -d "$1/../target/sandbox" ] \
		|| fail "Error: Development sandbox not found: $1/../target/sandbox. Run ./dev.sh build first."
}

sha256() {
	if command -v sha256sum >/dev/null 2>&1; then
		sha256sum "$1" | cut -d ' ' -f 1
	else
		shasum -a 256 "$1" | cut -d ' ' -f 1
	fi
}

valid_path() {
	# Release paths are deliberately restricted before passing them to tar or unzip.
	[[ "$1" =~ ^[A-Za-z0-9._+/-]+$ ]] || return 1
	case "/$1/" in
		*//*|*/./*|*/../*) return 1 ;;
	esac
}

parse_checksum() {
	[[ "$1" =~ ^[0-9a-f]{64}'  '.+$ ]] || fail "Invalid checksum entry: $1"
	checksum_digest=${1%%  *}
	checksum_name=${1#*  }
	valid_path "$checksum_name" || fail "Invalid checksum path: $checksum_name"
}

file_list() {
	(cd "$1" && find . -type f | sed 's|^./||' | sort)
}

write_manifest() {
	local directory=$1 name digest
	while IFS= read -r name; do
		[ "$name" = SHA256SUMS ] && continue
		valid_path "$name" || fail "Invalid release path: $name"
		digest=$(sha256 "$directory/$name")
		printf '%s  %s\n' "$digest" "$name"
	done < <(file_list "$directory")
}

verify_manifest() {
	local directory=$1 line
	local names=()
	[ -f "$directory/SHA256SUMS" ] || fail "Missing SHA256SUMS in $directory"
	while IFS= read -r line || [ -n "$line" ]; do
		parse_checksum "$line"
		[ -f "$directory/$checksum_name" ] || fail "Missing release file: $checksum_name"
		[ "$(sha256 "$directory/$checksum_name")" = "$checksum_digest" ] \
			|| fail "Release file checksum does not match: $checksum_name"
		names+=("$checksum_name")
	done < "$directory/SHA256SUMS"
	cmp -s <(printf '%s\n' "${names[@]}" | sort) \
		<(file_list "$directory" | sed '/^SHA256SUMS$/d') \
		|| fail "Release manifest must cover every file exactly once"
}

# Verify and adapt a release before replacing the prepared distribution.
import_release() (
	import_usage() {
		echo "Usage: $0 import ARCHIVE [--checksum FILE] [--destination DIRECTORY] [--sandbox]"
	}

	archive=
	checksum=
	destination="$WRAPPER_DIR"
	sandbox=false
	while [ "$#" -gt 0 ]; do
		case "$1" in
			--sandbox) sandbox=true; shift ;;
			--checksum|--destination)
				[ "$#" -ge 2 ] || fail "Missing value for $1"
				if [ "$1" = --checksum ]; then checksum=$2; else destination=$2; fi
				shift 2 ;;
			-h|--help) import_usage; exit 0 ;;
			-*) fail "Unknown option: $1" ;;
			*) [ -z "$archive" ] || fail "Only one archive may be imported"; archive=$1; shift ;;
		esac
	done
	[ -n "$archive" ] || { import_usage >&2; exit 1; }
	if [ "$sandbox" = true ]; then require_sandbox "$destination"; fi
	archive=$(cd "$(dirname "$archive")" && pwd)/$(basename "$archive")
	checksum=${checksum:-$archive.sha256}
	parse_checksum "$(cat "$checksum")"
	[ "$checksum_name" = "$(basename "$archive")" ] \
		&& [ "$(sha256 "$archive")" = "$checksum_digest" ] \
		|| fail "Release archive checksum does not match"
	[ -f "$destination/wrapper.conf" ] || fail "Missing $destination/wrapper.conf"
	destination=$(cd "$destination" && pwd)

	temporary=$(mktemp -d "${TMPDIR:-/tmp}/onedev-wrapper-import.XXXXXX")
	trap 'rm -rf "$temporary"' EXIT
	tar -tzf "$archive" > "$temporary/entries"
	root=
	while IFS= read -r entry; do
		name=${entry%/}
		valid_path "$name" || fail "Invalid archive path: $entry"
		[ -n "$root" ] || root=${name%%/*}
		[ "${name%%/*}" = "$root" ] || fail "Expected one release directory"
	done < "$temporary/entries"
	[ -n "$root" ] || fail "Empty release archive"
	[ -z "$(sort "$temporary/entries" | uniq -d)" ] || fail "Duplicate archive entry"
	# Reject symlinks, hard links, and special files before extracting anything.
	tar -tvzf "$archive" | awk 'substr($0, 1, 1) != "-" && substr($0, 1, 1) != "d" {exit 1}' \
		|| fail "Archive may contain only regular files and directories"
	mkdir "$temporary/extracted"
	tar -xzf "$archive" -C "$temporary/extracted"
	release="$temporary/extracted/$root"
	verify_manifest "$release"

	prepare_distribution "$release" "$destination" "$sandbox"
)

# Share OneDev adaptations between verified releases and local development builds.
prepare_distribution() (
	release=$1
	destination=$2
	[ -f "$destination/wrapper.conf" ] || fail "Missing $destination/wrapper.conf"
	sandbox=
	if [ "$3" = true ]; then
		require_sandbox "$destination"
		sandbox=$(cd "$destination/../target/sandbox" && pwd)
	fi
	temporary=$(mktemp -d "${TMPDIR:-/tmp}/onedev-wrapper-prepare.XXXXXX")
	trap 'rm -rf "$temporary"' EXIT
	for name in wrapper-linux-x86-64 wrapper-linux-arm-64 wrapper-macosx-arm-64 \
		wrapper-windows-x86-64.exe wrapper-windows-arm-64.exe wrapper.jar; do
		[ -f "$release/boot/$name" ] || fail "Missing release artifact: $name"
	done
	[ -f "$release/VERSION" ] || fail "Missing release VERSION"

	# Prepare all adaptations and checksums before replacing any checked-in files.
	prepared="$temporary/prepared"
	mkdir "$prepared"
	cp -R "$destination/." "$prepared/"
	distribution="$prepared/distribution"
	rm -rf "$distribution"
	mkdir -p "$distribution/bin" "$distribution/conf" "$distribution/agent/bin" "$distribution/agent/conf"
	cp -R "$release/boot" "$distribution/boot"
	cp -R "$distribution/boot" "$distribution/agent/boot"
	for extension in sh bat; do
		launcher="$release/bin/service.$extension"
		[ -f "$launcher" ] || fail "Missing release launcher: $launcher"
		[ "$(awk '{count += gsub(/ --script /, "&")} END {print count+0}' "$launcher")" = 1 ] \
			|| fail "Unrecognized launcher format: $launcher"
		cp "$launcher" "$distribution/bin/server.$extension"
		cp "$launcher" "$distribution/agent/bin/agent.$extension"
		for command in restore-db backup-db upgrade apply-db-constraints reset-admin-password enable-internal-login; do
			sed 's/ --script / --command /' "$launcher" > "$distribution/bin/$command.$extension"
		done
	done
	for kind in server agent; do
		if [ "$kind" = server ]; then
			prefix= main_class=io.onedev.commons.bootstrap.Bootstrap app_name=onedev.server display_name='OneDev Server' classpath=
		else
			prefix=agent/ main_class=io.onedev.agent.Agent app_name=onedev.agent display_name='OneDev Agent'
			classpath='wrapper.java.classpath.2=../lib/agentVersion/*.jar'
		fi
		configuration="$distribution/${prefix}conf/wrapper.conf"
		sed -e "s|@bootstrap\.class@|$main_class|g" -e "s|@app\.name@|$app_name|g" \
			-e "s|@app\.long\.name@|$display_name|g" -e "s|@app\.description@|$display_name|g" \
			-e "s|@classpath2@|$classpath|g" "$destination/wrapper.conf" > "$configuration"
		if grep -Eq '@[A-Za-z0-9_.]+@' "$configuration"; then
			fail "Unresolved placeholder in OneDev wrapper configuration"
		fi
	done
	while IFS= read -r name; do
		case "$name" in
			*.sh|*/wrapper-*) chmod 755 "$distribution/$name" ;;
			*) chmod 644 "$distribution/$name" ;;
		esac
	done < <(file_list "$distribution")
	cp "$release/VERSION" "$prepared/VERSION"
	write_manifest "$prepared" > "$prepared/SHA256SUMS"
	rm -rf "$destination/distribution"
	mv "$distribution" "$destination/distribution"
	cp "$prepared/VERSION" "$prepared/SHA256SUMS" "$destination/"
	echo "Imported Wrapper $(cat "$destination/VERSION") into $destination"
	if [ -n "$sandbox" ]; then
		refresh_sandbox "$destination/distribution" "$sandbox"
	fi
)

# Replace file entries instead of overwriting native executables in place.
refresh_sandbox() (
	distribution=$1
	sandbox=$2
	temporary=$(mktemp -d "$sandbox/.wrapper-update.XXXXXX")
	trap 'rm -rf "$temporary"' EXIT
	while IFS= read -r name; do
		case "$name" in
			boot/wrapper*|bin/*.sh|bin/*.bat|agent/boot/wrapper*|agent/bin/*.sh|agent/bin/*.bat)
				mkdir -p "$temporary/$(dirname "$name")"
				cp -p "$distribution/$name" "$temporary/$name"
				;;
		esac
	done < <(file_list "$distribution")
	while IFS= read -r name; do
		mkdir -p "$sandbox/$(dirname "$name")"
		mv -f "$temporary/$name" "$sandbox/$name"
	done < <(file_list "$temporary")
	echo "Updated wrapper binaries and scripts in $sandbox"
)

update_wrapper() (
	source=
	sandbox=false
	while [ "$#" -gt 0 ]; do
		case "$1" in
			--sandbox) sandbox=true ;;
			-h|--help) usage; exit 0 ;;
			-*) fail "Unknown option: $1" ;;
			*) [ -z "$source" ] || fail "Only one wrapper checkout may be imported"; source=$1 ;;
		esac
		shift
	done
	if [ "$sandbox" = true ]; then require_sandbox "$WRAPPER_DIR"; fi
	if [ -n "$source" ]; then
		update_local "$source" "$sandbox"
	else
		update_release "$sandbox"
	fi
)

# Overlay available local targets, retaining bundled binaries for unbuilt platforms.
update_local() (
	source=$1
	[ -d "$source" ] || fail "Wrapper checkout not found: $source"
	source=$(cd "$source" && pwd)
	[ -f "$source/dist/boot/wrapper.jar" ] \
		|| fail "Missing $source/dist/boot/wrapper.jar; run ./build.sh in $source first"
	built=false
	for name in wrapper-linux-x86-64 wrapper-linux-arm-64 wrapper-macosx-arm-64 \
		wrapper-windows-x86-64.exe wrapper-windows-arm-64.exe; do
		if [ -f "$source/dist/boot/$name" ]; then built=true; fi
	done
	[ "$built" = true ] || fail "No locally built wrapper binaries; run ./build.sh in $source first"
	version=$(awk '
		/^\[package\][[:space:]]*$/ {package=1; next}
		/^\[/ {package=0}
		package && /^[[:space:]]*version[[:space:]]*=/ {
			sub(/^[^"]*"/, ""); sub(/".*$/, ""); print; exit
		}' "$source/Cargo.toml")
	[ -n "$version" ] || fail "Missing package version in $source/Cargo.toml"

	temporary=$(mktemp -d "${TMPDIR:-/tmp}/onedev-wrapper-local.XXXXXX")
	trap 'rm -rf "$temporary"' EXIT
	release="$temporary/release"
	mkdir -p "$release/boot" "$release/bin"
	if [ -d "$WRAPPER_DIR/distribution/boot" ]; then
		cp -R "$WRAPPER_DIR/distribution/boot/." "$release/boot/"
	fi
	cp -R "$source/dist/boot/." "$release/boot/"
	cp "$source/resources/App.sh.in" "$release/bin/service.sh"
	cp "$source/resources/AppCommand.bat.in" "$release/bin/service.bat"
	printf '%s-local\n' "$version" > "$release/VERSION"
	echo "Importing local Wrapper build from $source"
	prepare_distribution "$release" "$WRAPPER_DIR" "$2"
)

# Download the latest published release, then reuse the local importer.
update_release() (
	site=https://code.onedev.io/onedev/wrapper/~sites/public/
	curl_options=(--fail --silent --show-error --location --connect-timeout 30)
	temporary=$(mktemp -d "${TMPDIR:-/tmp}/onedev-wrapper-download.XXXXXX")
	trap 'rm -rf "$temporary"' EXIT
	echo "Checking Wrapper releases at $site"
	curl "${curl_options[@]}" "$site" > "$temporary/index.html"
	# Each publication replaces the site with one release and its checksum.
	awk 'BEGIN {RS="<"}
		tolower($0) ~ /^a[[:space:]]/ {
			if (match($0, /[Hh][Rr][Ee][Ff][[:space:]]*=[[:space:]]*["\047]wrapper-[0-9][A-Za-z0-9.+-]*\.tar\.gz["\047]/)) {
				name = substr($0, RSTART, RLENGTH)
				sub(/^[^=]*=[[:space:]]*["\047]/, "", name)
				sub(/["\047]$/, "", name)
				print name
			}
		}' "$temporary/index.html" | sort -u > "$temporary/releases"
	[ "$(wc -l < "$temporary/releases" | tr -d ' ')" = 1 ] \
		|| fail "Expected exactly one Wrapper release on the public download site"
	name=$(cat "$temporary/releases")
	for filename in "$name" "$name.sha256"; do
		echo "Downloading $filename..."
		curl "${curl_options[@]}" --output "$temporary/$filename" "$site$filename"
	done
	if [ "$1" = true ]; then
		import_release "$temporary/$name" --sandbox
	else
		import_release "$temporary/$name"
	fi
)

# Verify Maven outputs, executable permissions, and incremental JSW cleanup.
check_package() (
	[ "$#" -ge 1 ] && [ "$#" -le 2 ] \
		|| fail "Usage: $0 check-package SERVER.zip [WRAPPER_INPUTS]"
	archive=$1
	inputs=${2:-$WRAPPER_DIR}
	agent=$(dirname "$archive")/agent
	distribution="$inputs/distribution"
	temporary=$(mktemp -d "${TMPDIR:-/tmp}/onedev-wrapper-check.XXXXXX")
	trap 'rm -rf "$temporary"' EXIT
	unzip -Z1 "$archive" > "$temporary/entries"
	prefix=
	while IFS= read -r name; do
		valid_path "${name%/}" || fail "Invalid package path: $name"
		[ -n "$prefix" ] || prefix=${name%%/*}/
		[ "${name%%/*}/" = "$prefix" ] || fail "Expected one package directory"
		case "$name" in *libwrapper-*) fail "Obsolete JSW file in package: $name" ;; esac
	done < "$temporary/entries"
	[ -n "$prefix" ] || fail "Empty server package"
	# Only extract the files being checked, preserving their executable permissions.
	unzip -q "$archive" "${prefix}boot/*" "${prefix}bin/*" "${prefix}conf/wrapper.conf" \
		"${prefix}agent/*" "${prefix}lib/io.onedev.server-product-*.jar" -d "$temporary/package"
	package="$temporary/package/${prefix%/}"
	while IFS= read -r name; do
		cmp -s "$distribution/$name" "$package/$name" || fail "Packaged file differs: $name"
		if [ -x "$distribution/$name" ]; then
			[ -x "$package/$name" ] || fail "Missing executable permission: $name"
		fi
	done < <(file_list "$distribution")
	while IFS= read -r name; do
		cmp -s "$distribution/boot/$name" "$agent/boot/$name" || fail "Agent boot file differs: $name"
		if [ -x "$distribution/boot/$name" ]; then
			[ -x "$agent/boot/$name" ] || fail "Missing agent executable permission: $name"
		fi
	done < <(file_list "$distribution/boot")
	for name in server upgrade backup-db restore-db reset-admin-password apply-db-constraints enable-internal-login; do
		if [ "$name" = server ]; then mode=--script; else mode=--command; fi
		for extension in sh bat; do
			grep -q -- "$mode" "$package/bin/$name.$extension" || fail "Wrong launcher mode: $name.$extension"
		done
	done
	for name in conf/wrapper.conf agent/conf/wrapper.conf; do
		if grep -Eq '@bootstrap.class@|@app.name@' "$package/$name"; then
			fail "Unresolved configuration placeholder: $name"
		fi
	done
	[ -f "$agent/conf/wrapper.conf" ] || fail "Missing agent configuration"
	if grep -q agentVersion "$agent/conf/wrapper.conf"; then fail "Unresolved agent version"; fi
	for name in "$agent"/boot/libwrapper-*; do
		[ ! -e "$name" ] || fail "Obsolete JSW file in agent: $name"
	done
	# The product jar must not accidentally embed a populated development sandbox.
	for product in "$package"/lib/io.onedev.server-product-*.jar; do
		unzip -Z1 "$product" > "$temporary/product-entries"
		if grep -Eq '^sandbox/(lib/|boot/wrapper|boot/libwrapper|bin/)' "$temporary/product-entries"; then
			fail "Product jar contains development sandbox files"
		fi
		grep -qx sandbox/release.properties "$temporary/product-entries" || fail "Missing sandbox/release.properties"
	done
	verify_manifest "$inputs"
	echo "Server archive, agent distribution, embedded product jar, permissions, and checksums verified"
)

# Exercise the maintenance commands without native builds or network access.
test_wrapper() (
	temporary=$(mktemp -d "${TMPDIR:-/tmp}/onedev-wrapper-test.XXXXXX")
	trap 'rm -rf "$temporary"' EXIT
	export COPYFILE_DISABLE=1
	fixture="$temporary/release/wrapper-1.0.0"
	destination="$temporary/wrapper inputs"
	archive="$temporary/wrapper-1.0.0.tar.gz"
	mkdir -p "$fixture/boot" "$fixture/bin" "$destination"
	cp "$WRAPPER_DIR/wrapper.conf" "$destination/wrapper.conf"
	for name in wrapper-linux-x86-64 wrapper-linux-arm-64 wrapper-macosx-arm-64 \
		wrapper-windows-x86-64.exe wrapper-windows-arm-64.exe wrapper.jar; do
		printf 'binary\000\377' > "$fixture/boot/$name"
	done
	printf '#!/bin/bash\nexec binary --script "$0" "$@"\n' > "$fixture/bin/service.sh"
	printf '@echo off\r\nbinary --script "%%~n0" %%*\r\n' > "$fixture/bin/service.bat"
	printf '1.0.0\n' > "$fixture/VERSION"

	pack_release() {
		write_manifest "$fixture" > "$fixture/SHA256SUMS"
		tar -czf "$archive" -C "$temporary/release" wrapper-1.0.0
		printf '%s  %s\n' "$(sha256 "$archive")" "$(basename "$archive")" > "$archive.sha256"
	}

	run_import() {
		"$BASH" "$SCRIPT" import "$archive" --destination "$destination"
	}

	expect_rejection() {
		if run_import > "$temporary/error" 2>&1; then fail "Invalid release was accepted"; fi
		diff -r "$temporary/before" "$destination" || fail "Invalid release modified prepared files"
	}

	# New boot artifacts must reach both distributions; retired files must disappear.
	printf 'future format\000\377' > "$fixture/boot/new-runtime.dat"
	pack_release
	run_import
	for prefix in '' agent/; do
		cmp "$fixture/boot/new-runtime.dat" "$destination/distribution/${prefix}boot/new-runtime.dat"
	done
	touch "$destination/distribution/bin/retired-command.sh"
	rm "$fixture/boot/new-runtime.dat"
	pack_release
	run_import
	for name in bin/retired-command.sh boot/new-runtime.dat agent/boot/new-runtime.dat; do
		[ ! -e "$destination/distribution/$name" ] || fail "Retired file survived: $name"
	done
	cmp "$WRAPPER_DIR/wrapper.conf" "$destination/wrapper.conf"

	# Preserve binary bytes, CRLF launchers, substitutions, modes, and manifest coverage.
	distribution="$destination/distribution"
	cmp "$fixture/bin/service.sh" "$distribution/bin/server.sh"
	cmp "$fixture/bin/service.bat" "$distribution/agent/bin/agent.bat"
	sed 's/ --script / --command /' "$fixture/bin/service.bat" > "$temporary/command.bat"
	cmp "$temporary/command.bat" "$distribution/bin/backup-db.bat"
	grep -q ' --command ' "$distribution/bin/backup-db.sh"
	[ -x "$distribution/bin/backup-db.sh" ]
	[ -x "$distribution/boot/wrapper-linux-x86-64" ]
	[ ! -x "$distribution/boot/wrapper.jar" ]
	grep -q io.onedev.commons.bootstrap.Bootstrap "$distribution/conf/wrapper.conf"
	grep -q io.onedev.agent.Agent "$distribution/agent/conf/wrapper.conf"
	grep -Fq '../lib/agentVersion/*.jar' "$distribution/agent/conf/wrapper.conf"
	if grep -Eq '@[A-Za-z0-9_.]+@' "$distribution/conf/wrapper.conf" "$distribution/agent/conf/wrapper.conf"; then
		fail "Unresolved placeholder"
	fi
	verify_manifest "$destination"
	cp -R "$destination" "$temporary/before"

	# Bad downloads and invalid release contents must leave the previous import intact.
	printf '%064d  %s\n' 0 "$(basename "$archive")" > "$archive.sha256"
	expect_rejection
	cp "$fixture/bin/service.sh" "$temporary/launcher"
	printf 'unknown launcher format\n' > "$fixture/bin/service.sh"
	pack_release
	expect_rejection
	cp "$temporary/launcher" "$fixture/bin/service.sh"
	mv "$fixture/boot/wrapper-linux-x86-64" "$temporary/binary"
	pack_release
	expect_rejection
	mv "$temporary/binary" "$fixture/boot/wrapper-linux-x86-64"
	pack_release
	printf 'tampered\n' >> "$fixture/boot/wrapper.jar"
	tar -czf "$archive" -C "$temporary/release" wrapper-1.0.0
	printf '%s  %s\n' "$(sha256 "$archive")" "$(basename "$archive")" > "$archive.sha256"
	expect_rejection
	printf 'binary\000\377' > "$fixture/boot/wrapper.jar"
	ln -s "$temporary/binary" "$fixture/boot/link"
	pack_release
	expect_rejection
	rm "$fixture/boot/link"
	pack_release
	sed '/  VERSION$/d' "$fixture/SHA256SUMS" > "$temporary/incomplete-manifest"
	cp "$temporary/incomplete-manifest" "$fixture/SHA256SUMS"
	tar -czf "$archive" -C "$temporary/release" wrapper-1.0.0
	printf '%s  %s\n' "$(sha256 "$archive")" "$(basename "$archive")" > "$archive.sha256"
	expect_rejection
	echo "Wrapper importer tests passed"

	# Exercise the downloader offline, including multiple links on the same HTML line.
	pack_release
	mkdir -p "$temporary/wrapper" "$temporary/mock-bin" "$temporary/downloads"
	cp "$SCRIPT" "$WRAPPER_DIR/wrapper.conf" "$temporary/wrapper/"
	cat > "$temporary/mock-bin/curl" <<-'MOCK'
	#!/bin/bash
	set -euo pipefail
	output=
	while [ "$#" -gt 1 ]; do
		case "$1" in
			--output) output=$2; shift 2 ;;
			--connect-timeout) shift 2 ;;
			*) shift ;;
		esac
	done
	if [ -n "$output" ]; then
		cp "$WRAPPER_TEST_FIXTURE/${1##*/}" "$output"
	else
		cat "$WRAPPER_TEST_FIXTURE/index.html"
	fi
	MOCK
	chmod +x "$temporary/mock-bin/curl"
	export WRAPPER_TEST_FIXTURE="$temporary"
	printf '<a href="wrapper-1.0.0.tar.gz">release</a><a HREF=\047wrapper-1.0.0.tar.gz\047>duplicate</a>\n' \
		> "$temporary/index.html"
	PATH="$temporary/mock-bin:$PATH" TMPDIR="$temporary/downloads" "$temporary/wrapper/update.sh"
	verify_manifest "$temporary/wrapper"
	[ -z "$(ls -A "$temporary/downloads")" ] || fail "Downloads were not cleaned up"
	printf '<a href="wrapper-2.0.0.tar.gz">another release</a>\n' >> "$temporary/index.html"
	if PATH="$temporary/mock-bin:$PATH" TMPDIR="$temporary/downloads" "$temporary/wrapper/update.sh" \
		> "$temporary/error" 2>&1; then
		fail "Ambiguous download site was accepted"
	fi
	[ -z "$(ls -A "$temporary/downloads")" ] || fail "Failed download was not cleaned up"
	echo "Wrapper updater tests passed"

	# Build a small Maven-shaped package and check that content/mode regressions fail.
	mkdir -p "$temporary/package/onedev-test" "$temporary/product/sandbox"
	package="$temporary/package/onedev-test"
	cp -R "$distribution/." "$package/"
	cp -R "$distribution/agent" "$temporary/agent"
	sed 's/agentVersion/1.0.0/g' "$distribution/agent/conf/wrapper.conf" > "$temporary/agent/conf/wrapper.conf"
	mkdir "$package/lib"
	printf 'version=1.0.0\n' > "$temporary/product/sandbox/release.properties"
	(cd "$temporary/product" && zip -qr "$package/lib/io.onedev.server-product-1.0.0.jar" sandbox)
	pack_server() {
		rm -f "$temporary/server.zip"
		(cd "$temporary/package" && zip -qr "$temporary/server.zip" onedev-test)
	}
	check_server() {
		"$BASH" "$SCRIPT" check-package "$temporary/server.zip" "$destination"
	}
	expect_bad_package() {
		if check_server > "$temporary/error" 2>&1; then fail "Invalid package was accepted"; fi
	}
	pack_server
	check_server
	printf 'corrupt\n' >> "$package/boot/wrapper.jar"
	pack_server
	expect_bad_package
	cp "$distribution/boot/wrapper.jar" "$package/boot/wrapper.jar"
	chmod 644 "$package/bin/server.sh"
	pack_server
	expect_bad_package
	chmod 755 "$package/bin/server.sh"
	touch "$package/boot/libwrapper-obsolete.so"
	pack_server
	expect_bad_package
	rm "$package/boot/libwrapper-obsolete.so"
	pack_server
	chmod 644 "$temporary/agent/boot/wrapper-linux-x86-64"
	expect_bad_package
	chmod 755 "$temporary/agent/boot/wrapper-linux-x86-64"
	mkdir -p "$temporary/product/sandbox/lib"
	touch "$temporary/product/sandbox/lib/old.jar"
	(cd "$temporary/product" && zip -qr "$package/lib/io.onedev.server-product-1.0.0.jar" sandbox)
	pack_server
	expect_bad_package
	echo "Wrapper package checker tests passed"
)

command=${1:-update}
[ "$#" -eq 0 ] || shift
case "$command" in
	update) update_wrapper "$@" ;;
	import) import_release "$@" ;;
	check-package) check_package "$@" ;;
	test)
		[ "$#" -eq 0 ] || fail "Usage: $0 test"
		test_wrapper
		;;
	help|-h|--help) usage ;;
	*) update_wrapper "$command" "$@" ;;
esac
