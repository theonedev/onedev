#!/bin/bash

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

MAVEN_OPTS="--add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.desktop/java.beans=ALL-UNNAMED --add-modules=java.se --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED --add-opens=java.management/sun.management=ALL-UNNAMED --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED --add-opens=java.base/sun.nio.fs=ALL-UNNAMED"
export MAVEN_OPTS

HOTSWAP_AGENT_VERSION=2.0.3
ECJ_VERSION=3.38.0
MAVEN_REPOSITORY="${MAVEN_REPOSITORY:-$HOME/.m2/repository}"

run_maven() {
	if [ -f server-ee/pom.xml ]; then
		mvn "$@"
	else
		mvn -Pce "$@"
	fi
}

artifact_path() {
	group_path=$(echo "$1" | tr . /)
	echo "$MAVEN_REPOSITORY/$group_path/$2/$3/$2-$3.jar"
}

ensure_artifact() {
	artifact=$(artifact_path "$1" "$2" "$3")
	if [ ! -f "$artifact" ]; then
		echo "Downloading $1:$2:$3..." >&2
		run_maven -q -U dependency:get -Dmaven.repo.local="$MAVEN_REPOSITORY" \
			-Dartifact="$1:$2:$3:jar" -Dtransitive=false
	fi
	if [ ! -f "$artifact" ]; then
		echo "Unable to find $1:$2:$3 in $MAVEN_REPOSITORY" >&2
		return 1
	fi
	echo "$artifact"
}

build_classpath() {
	deps_classpath_file=server-product/target/deps-classpath.txt
	if [ ! -s "$deps_classpath_file" ]; then
		# Sibling modules are only resolvable from the reactor, so include them with
		# -am and let the compile phase run. Compilation itself is skipped as classes
		# are built separately, but Maven still needs the phase to map module
		# dependencies to their target/classes directories instead of looking for
		# jars in a repository.
		run_maven -pl server-product -am compile dependency:build-classpath \
			-Dmaven.main.skip=true -Dmdep.outputFile=target/deps-classpath.txt || return 1
	fi

	module_cp=$(find . -path '*/target/classes' -type d ! -path '*/bin/*' \
		| tr '\n' ':' | sed 's/:$//')
	local_artifacts=$(find . -path '*/target/classes' -type d ! -path '*/bin/*' \
		| while read -r dir; do basename "$(dirname "$(dirname "$dir")")"; done | sort -u)
	deps_cp=$(tr ':' '\n' < "$deps_classpath_file" | while read -r jar; do
		if [ "${jar#$ROOT/}" != "$jar" ]; then
			continue
		fi
		artifact=$(echo "$jar" | sed -n 's|.*/io/onedev/\([^/]*\)/.*|\1|p')
		if [ -n "$artifact" ] && echo "$local_artifacts" | grep -qx "$artifact"; then
			continue
		fi
		echo "$jar"
	done | tr '\n' ':' | sed 's/:$//')
	classpath="$module_cp:$deps_cp"
}

compile_changed() {
	work_dir=$(mktemp -d "${TMPDIR:-/tmp}/onedev-compile.XXXXXX")
	sources="$work_dir/sources"
	: > "$sources"

	if [ "$#" -gt 0 ]; then
		for source in "$@"; do
			case "$source" in
				*.java) ;;
				*) echo "Not a Java source file: $source" >&2; exit 1 ;;
			esac
		if [ ! -f "$source" ]; then
			echo "Java source file does not exist: $source" >&2
			exit 1
		fi
		case "$source" in
			/*) echo "$source" ;;
			*) echo "$ROOT/${source#./}" ;;
		esac >> "$sources"
		done
	else
		find . -path '*/src/main/java/*.java' -type f \
			! -path '*/archetype-resources/*' \
			| while read -r source; do
			module=${source%%/src/main/java/*}
			relative=${source#*/src/main/java/}
			class="$module/target/classes/${relative%.java}.class"
			if [ ! -f "$class" ] || [ "$source" -nt "$class" ]; then
				echo "$ROOT/${source#./}"
			fi
		done > "$sources"
	fi

	if [ ! -s "$sources" ]; then
		echo "No changed Java source files to compile."
		rm -rf "$work_dir"
		return
	fi

	ecj=$(ensure_artifact org.eclipse.jdt ecj "$ECJ_VERSION") || {
		rm -rf "$work_dir"
		return 1
	}
	if [ -z "${classpath:-}" ]; then
		build_classpath || {
			rm -rf "$work_dir"
			return 1
		}
	fi

	sed 's|/src/main/java/.*||' "$sources" | sort -u | while read -r module; do
		module_sources="$work_dir/$(echo "$module" | sed 's|/|_|g').sources"
		grep "^$module/src/main/java/" "$sources" > "$module_sources"
		mkdir -p "$module/target/classes"
		count=$(wc -l < "$module_sources" | tr -d ' ')
		echo "Compiling $count changed file(s) in ${module#$ROOT/}..."
		java -jar "$ecj" -17 -encoding UTF-8 -g -parameters -proc:none -nowarn \
			-classpath "$classpath" -d "$module/target/classes" "@$module_sources" \
			|| return 1
	done || {
		rm -rf "$work_dir"
		return 1
	}

	rm -rf "$work_dir"
}

copy_changed_resources() {
	work_dir=$(mktemp -d "${TMPDIR:-/tmp}/onedev-resources.XXXXXX")
	resources="$work_dir/resources"
	: > "$resources"

	find . -type f ! -path '*/bin/*' \
		! -name '.DS_Store' ! -name '.*.sw?' ! -name '*~' \( \
		\( -path '*/src/main/java/*' ! -name '*.java' \) -o \
		-path '*/src/main/resources/*' \
	\) | while read -r source; do
		case "$source" in
		*/src/main/resources/*)
			module=${source%%/src/main/resources/*}
			relative=${source#*/src/main/resources/}
			;;
		*/src/main/java/*)
			module=${source%%/src/main/java/*}
			relative=${source#*/src/main/java/}
			;;
		esac
		target="$module/target/classes/$relative"
		if [ ! -f "$target" ] || [ "$source" -nt "$target" ]; then
			echo "$module $source" >> "$resources"
		fi
	done

	if [ -s "$resources" ]; then
		modules=$(cut -d ' ' -f 1 "$resources" | sed 's|^./||' | sort -u | paste -sd, -)
		if ! run_maven -q -pl "$modules" resources:resources; then
			rm -rf "$work_dir"
			return 1
		fi
		cut -d ' ' -f 2- "$resources" | while read -r source; do
			echo "Copied resource: ${source#./}"
		done
	fi

	rm -rf "$work_dir"
}

watch_and_build() {
	reference=$1
	while true; do
		sleep 1
		if find . -type f ! -path '*/target/*' ! -path '*/bin/*' \
			! -name '.DS_Store' ! -name '.*.sw?' ! -name '*~' \( \
			-name pom.xml -o -path '*/src/main/java/*' -o \
			-path '*/src/main/resources/*' \
		\) -newer "$reference" -print -quit | grep -q .; then
			detected=$(mktemp "${TMPDIR:-/tmp}/onedev-change.XXXXXX")
			touch "$detected"
			echo >&2
			echo "Changes detected. Running incremental build..." >&2
			if ! build_project; then
				echo "Build failed. Waiting for further changes..." >&2
			fi
			touch -r "$detected" "$reference"
			rm -f "$detected"
		fi
	done
}

compile_project() {
	build_reference="server-product/target/sandbox"
	rm -f server-product/target/deps-classpath.txt
	run_maven compile "$@" || return 1
	if [ ! -d "$build_reference" ]; then
		echo "Maven compile did not create $build_reference" >&2
		return 1
	fi
	touch "$build_reference"
}

restore_preserved_sandbox() {
	if [ -n "${preserved_sandbox:-}" ] && [ -d "$preserved_sandbox" ]; then
		mkdir -p "$ROOT/server-product/target"
		if [ -e "$ROOT/server-product/target/sandbox" ]; then
			echo "Unable to restore sandbox: destination already exists" >&2
			return 1
		fi
		mv "$preserved_sandbox" "$ROOT/server-product/target/sandbox" || return 1
		rmdir "$preservation_dir" || return 1
		preserved_sandbox=
		preservation_dir=
	fi
}

rebuild_project() {
	sandbox="$ROOT/server-product/target/sandbox"
	preservation_dir=
	preserved_sandbox=
	sandbox_preserved=false
	if [ -d "$sandbox" ]; then
		preservation_dir=$(mktemp -d "$ROOT/server-product/.onedev-sandbox.XXXXXX")
		preserved_sandbox="$preservation_dir/sandbox"
		trap restore_preserved_sandbox EXIT
		if ! mv "$sandbox" "$preserved_sandbox"; then
			trap - EXIT
			rmdir "$preservation_dir"
			return 1
		fi
		sandbox_preserved=true
	fi

	clean_status=0
	run_maven clean "$@" || clean_status=$?
	if ! restore_preserved_sandbox; then
		return 1
	fi
	if [ "$sandbox_preserved" = true ]; then
		trap - EXIT
	fi
	if [ "$clean_status" -ne 0 ]; then
		return "$clean_status"
	fi

	compile_project "$@"
}

build_project() {
	build_reference="server-product/target/sandbox"
	if [ ! -d "$build_reference" ]; then
		echo "Development sandbox not found. Running: mvn compile"
		compile_project || return 1
		classpath=
		return
	fi

	changed_poms=$(find . -name pom.xml -type f ! -path '*/target/*' \
		! -path '*/archetype-resources/*' -newer "$build_reference" | sort)
	if [ -n "$changed_poms" ]; then
		rm -f server-product/target/deps-classpath.txt
		if echo "$changed_poms" | grep -qx './pom.xml'; then
			echo "Root pom.xml changed. Running: mvn compile"
			run_maven compile || return 1
		else
			modules=$(echo "$changed_poms" | while read -r pom; do
				dirname "${pom#./}"
			done | sort -u | paste -sd, -)
			echo "Module POM changes detected. Running: mvn -pl $modules -am -amd compile"
			run_maven -pl "$modules" -am -amd compile || return 1
		fi
		classpath=
		touch "$build_reference"
		return
	fi

	copy_changed_resources || return 1
	compile_changed "$@" || return 1
	touch "$build_reference"
}

usage() {
	echo "Usage: ./dev.sh <command> [<command>...] [options]"
	echo
	echo "Commands:"
	echo "  run      Build, start the dev server, and rebuild automatically when files change"
	echo "  build    Build with Maven when needed, otherwise compile changed files with ECJ"
	echo "  rebuild  Clean and build all modules while preserving the development sandbox"
	echo "  test     Run tests with Maven"
	echo "  package  Package all modules with Maven"
	echo "  install  Install project artifacts with Maven"
	echo "  clean    Clean build outputs with Maven"
	echo
	echo "Commands may be combined, for example: ./dev.sh clean package"
}

is_command() {
	case "$1" in
		build|rebuild|clean|install|test|package|run) return 0 ;;
		*) return 1 ;;
	esac
}

flush_maven_commands() {
	if [ "${#maven_commands[@]}" -gt 0 ]; then
		run_maven "${maven_commands[@]}" "${maven_arguments[@]}"
		maven_commands=()
		maven_arguments=()
	fi
}

if [ "$#" -eq 0 ]; then
	usage
	exit 1
fi

maven_commands=()
maven_arguments=()
run_requested=false
run_arguments=()
while [ "$#" -gt 0 ]; do
	command=$1
	shift
	case "$command" in
		clean|install|test|package)
			maven_commands+=("$command")
			while [ "$#" -gt 0 ] && ! is_command "$1"; do
				maven_arguments+=("$1")
				shift
			done
			;;
		build|rebuild)
			flush_maven_commands
			command_arguments=()
			while [ "$#" -gt 0 ] && ! is_command "$1"; do
				command_arguments+=("$1")
				shift
			done
			if [ "$command" = build ]; then
				build_project "${command_arguments[@]}"
			else
				rebuild_project "${command_arguments[@]}"
			fi
			;;
		run)
			flush_maven_commands
			run_requested=true
			run_arguments=("$@")
			break
			;;
		*)
			usage >&2
			exit 1
			;;
	esac
done
flush_maven_commands

if [ "$run_requested" != true ]; then
	exit
fi

set -- "${run_arguments[@]}"
watch_reference=$(mktemp "${TMPDIR:-/tmp}/onedev-watch.XXXXXX")
touch "$watch_reference"
trap 'rm -f "$watch_reference"' EXIT
build_project

if [ -z "${classpath:-}" ]; then
	build_classpath
fi
hotswap_agent=$(ensure_artifact org.hotswapagent hotswap-agent "$HOTSWAP_AGENT_VERSION")

hotswap_options="-javaagent:$hotswap_agent=autoHotswap=true"
if java -XX:+AllowEnhancedClassRedefinition -version >/dev/null 2>&1; then
	hotswap_options="-XX:+AllowEnhancedClassRedefinition $hotswap_options"
else
	echo "Warning: this JVM only supports hot loading method-body changes." >&2
	echo "Use a JetBrains Runtime with AllowEnhancedClassRedefinition for structural changes." >&2
fi

echo "HotswapAgent enabled. Watching source files for changes." >&2
watch_and_build "$watch_reference" &
watcher_pid=$!
cleanup_run() {
	kill "$watcher_pid" 2>/dev/null || true
	wait "$watcher_pid" 2>/dev/null || true
	rm -f "$watch_reference"
}
trap cleanup_run EXIT

java $MAVEN_OPTS $hotswap_options -cp "$classpath" \
	io.onedev.commons.bootstrap.Bootstrap "$@"
