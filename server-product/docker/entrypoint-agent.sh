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

cd /agent/boot
./wrapper-linux-$CPUARCH /agent/conf/wrapper.conf &

child=$!
wait "$child"
