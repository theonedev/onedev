#!/bin/bash

set -e 

_sigterm() {
  kill -TERM "$child"
  wait "$child"
  exit 1
}

trap _sigterm SIGTERM
trap _sigterm SIGINT

MOD_OPTIONS="--add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED --add-modules=java.se --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED --add-opens=java.management/sun.management=ALL-UNNAMED --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED"

MEM_OPTIONS=-XX:MaxRAMPercentage=50.0
if [[ $JAVA_TOOL_OPTIONS == *"-XX:MaxRAMPercentage"* ]] || [[ $JAVA_TOOL_OPTIONS == *"-Xmx"* ]]; then
  unset MEM_OPTIONS
fi

cd /app/bin
java -cp "../boot/*" ${MEM_OPTIONS} ${MOD_OPTIONS} io.onedev.commons.bootstrap.Bootstrap upgrade /opt/onedev &
child=$!
wait "$child"

touch /opt/onedev/IN_DOCKER

cd /opt/onedev/bin
java -cp "../boot/*" ${MEM_OPTIONS} ${MOD_OPTIONS} io.onedev.commons.bootstrap.Bootstrap &
child=$!
wait "$child"
