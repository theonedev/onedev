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
		run_maven -U dependency:get -Dmaven.repo.local="$MAVEN_REPOSITORY" \
			-Dartifact="$1:$2:$3:jar" -Dtransitive=false
	fi
	if [ ! -f "$artifact" ]; then
		echo "Unable to find $1:$2:$3 in $MAVEN_REPOSITORY" >&2
		exit 1
	fi
	echo "$artifact"
}

build_classpath() {
	run_maven -pl server-product dependency:build-classpath \
		-Dmdep.outputFile=target/deps-classpath.txt

	module_cp=$(find . -path '*/target/classes' -type d ! -path '*/bin/*' \
		| tr '\n' ':' | sed 's/:$//')
	local_artifacts=$(find . -path '*/target/classes' -type d ! -path '*/bin/*' \
		| while read -r dir; do basename "$(dirname "$(dirname "$dir")")"; done | sort -u)
	deps_cp=$(tr ':' '\n' < server-product/target/deps-classpath.txt | while read -r jar; do
		artifact=$(echo "$jar" | sed -n 's|.*/io/onedev/\([^/]*\)/.*|\1|p')
		if [ -n "$artifact" ] && echo "$local_artifacts" | grep -qx "$artifact"; then
			continue
		fi
		echo "$jar"
	done | tr '\n' ':' | sed 's/:$//')
	classpath="$module_cp:$deps_cp"
}

compile_changed() {
	ecj=$(ensure_artifact org.eclipse.jdt ecj "$ECJ_VERSION")
	build_classpath

	work_dir=$(mktemp -d "${TMPDIR:-/tmp}/onedev-compile.XXXXXX")
	trap 'rm -rf "$work_dir"' EXIT
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
		return
	fi

	sed 's|/src/main/java/.*||' "$sources" | sort -u | while read -r module; do
		module_sources="$work_dir/$(echo "$module" | sed 's|/|_|g').sources"
		grep "^$module/src/main/java/" "$sources" > "$module_sources"
		mkdir -p "$module/target/classes"
		count=$(wc -l < "$module_sources" | tr -d ' ')
		echo "Compiling $count changed file(s) in ${module#$ROOT/}..."
		java -jar "$ecj" -17 -encoding UTF-8 -g -parameters -proc:none -nowarn \
			-classpath "$classpath" -d "$module/target/classes" "@$module_sources"
	done
}

build_project() {
	build_reference="server-product/target/sandbox"

	if [ ! -d "$build_reference" ]; then
		echo "Development sandbox not found. Running: mvn compile"
		run_maven compile
		if [ ! -d "$build_reference" ]; then
			echo "Maven compile did not create $build_reference" >&2
			exit 1
		fi
		touch "$build_reference"
		return
	fi

	changed_poms=$(find . -name pom.xml -type f ! -path '*/target/*' \
		! -path '*/archetype-resources/*' -newer "$build_reference" | sort)
	if [ -n "$changed_poms" ]; then
		if echo "$changed_poms" | grep -qx './pom.xml'; then
			echo "Root pom.xml changed. Running: mvn compile"
			run_maven compile
		else
			modules=$(echo "$changed_poms" | while read -r pom; do
				dirname "${pom#./}"
			done | sort -u | paste -sd, -)
			echo "Module POM changes detected. Running: mvn -pl $modules -am -amd compile"
			run_maven -pl "$modules" -am -amd compile
		fi
		touch "$build_reference"
		return
	fi

	compile_changed "$@"
	touch "$build_reference"
}

usage() {
	echo "Usage: ./dev.sh <command>"
	echo
	echo "Commands:"
	echo "  run      Start the dev server and hot-load rebuilt classes; restart if hot loading fails"
	echo "  build    Build with Maven when needed, otherwise compile changed files with ECJ"
	echo "  clean    Clean build outputs with Maven"
}

if [ "$#" -eq 0 ]; then
	usage
	exit 1
fi

case "$1" in
	build)
		shift
		build_project "$@"
		exit
		;;
	clean)
		shift
		run_maven clean "$@"
		exit
		;;
	run)
		shift
		;;
	*)
		usage >&2
		exit 1
		;;
esac

build_classpath
hotswap_agent=$(ensure_artifact org.hotswapagent hotswap-agent "$HOTSWAP_AGENT_VERSION")

hotswap_options="-javaagent:$hotswap_agent=autoHotswap=true"
if java -XX:+AllowEnhancedClassRedefinition -version >/dev/null 2>&1; then
	hotswap_options="-XX:+AllowEnhancedClassRedefinition $hotswap_options"
else
	echo "Warning: this JVM only supports hot loading method-body changes." >&2
	echo "Use a JetBrains Runtime with AllowEnhancedClassRedefinition for structural changes." >&2
fi

echo "HotswapAgent enabled. Run './dev.sh build' after changing Java files." >&2
exec java $MAVEN_OPTS $hotswap_options -cp "$classpath" \
	io.onedev.commons.bootstrap.Bootstrap "$@"
