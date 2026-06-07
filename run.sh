#!/bin/bash

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

export MAVEN_OPTS="--add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED --add-modules=java.se --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED --add-opens=java.management/sun.management=ALL-UNNAMED --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED --add-opens=java.base/sun.nio.fs=ALL-UNNAMED"

mvn -pl server-product -am -q compile
mvn -pl server-product -q dependency:build-classpath -Dmdep.outputFile=target/deps-classpath.txt

module_cp=$(find . -path '*/target/classes' -type d ! -path '*/bin/*' | tr '\n' ':' | sed 's/:$//')
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

exec java $MAVEN_OPTS -cp "$classpath" io.onedev.commons.bootstrap.Bootstrap "$@"
