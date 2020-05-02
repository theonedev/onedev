#!/bin/bash

_sigterm() {
  kill -TERM "$child"
  wait "$child"
  exit 1
}
_sigint() {
  wait "$child"
  exit 1
}

trap _sigterm SIGTERM
trap _sigint SIGINT

/app/boot/wrapper-linux-x86-64 /app/conf/wrapper.conf wrapper.pidfile=/app/status/onedev_upgrade.pid -- upgrade /opt/onedev & 
child=$!
wait "$child"

touch /opt/onedev/IN_DOCKER
/opt/onedev/boot/wrapper-linux-x86-64 /opt/onedev/conf/wrapper.conf &
child=$!
wait "$child"
