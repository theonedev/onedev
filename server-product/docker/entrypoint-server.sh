#!/bin/bash

set -e 

_sigterm() {
  kill -TERM "$child"
  wait "$child"
  exit 1
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
/opt/onedev/boot/wrapper-linux-$CPUARCH /opt/onedev/conf/wrapper.conf &
child=$!
wait "$child"
