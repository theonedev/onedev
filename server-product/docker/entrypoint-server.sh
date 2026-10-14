#!/bin/bash

set -e 

_sigterm() {
  kill -TERM "$child" 2>/dev/null || true
  wait "$child" 2>/dev/null || true
  exit 143
}

trap _sigterm SIGTERM
trap _sigterm SIGINT

UNAME=`uname -m`

if [[ $UNAME == "aarch64" ]]; then
    CPUARCH="arm-64"
else
    CPUARCH="x86-64"
fi

/app/boot/wrapper-linux-$CPUARCH /app/conf/wrapper.conf -- upgrade /opt/onedev &
child=$!
wait "$child"

touch /opt/onedev/IN_DOCKER
trap - SIGTERM SIGINT
exec /opt/onedev/boot/wrapper-linux-$CPUARCH /opt/onedev/conf/wrapper.conf
