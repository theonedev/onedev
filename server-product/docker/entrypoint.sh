#!/bin/bash

set -e 

_sigterm() {
  kill -TERM "$child"
  wait "$child"
  exit 1
}

trap _sigterm SIGTERM
trap _sigterm SIGINT

JVM_OPTIONS=-XX:MaxRAMPercentage=50.0
if [[ $JAVA_TOOL_OPTIONS == *"-XX:MaxRAMPercentage"* ]] || [[ $JAVA_TOOL_OPTIONS == *"-Xmx"* ]]; then
  unset JVM_OPTIONS
fi

cd /app/bin
java -cp "../boot/*" ${JVM_OPTIONS} io.onedev.commons.bootstrap.Bootstrap upgrade /opt/onedev &
child=$!
wait "$child"

touch /opt/onedev/IN_DOCKER

cd /opt/onedev/bin
java -cp "../boot/*" ${JVM_OPTIONS} io.onedev.commons.bootstrap.Bootstrap &
child=$!
wait "$child"
