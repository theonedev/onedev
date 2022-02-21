#!/bin/bash

set -e 

_sigterm() {
  kill -TERM "$child"
  wait "$child"
  exit 1
}

trap _sigterm SIGTERM
trap _sigterm SIGINT

cd /app/bin
java -cp "../boot/*" -XX:MaxRAMPercentage=50.0 io.onedev.commons.bootstrap.Bootstrap upgrade /opt/onedev &
child=$!
wait "$child"

touch /opt/onedev/IN_DOCKER

cd /opt/onedev/bin
java -cp "../boot/*" -XX:MaxRAMPercentage=50.0 io.onedev.commons.bootstrap.Bootstrap &
child=$!
wait "$child"
