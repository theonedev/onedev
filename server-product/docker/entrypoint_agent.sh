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
CPUARCH=""

if [[ $UNAME == "x86_64" ]]; then
    CPUARCH="x86-64"
elif [[ $UNAME == "aarch64" ]]; then
    CPUARCH="arm-64"
fi

/agent/boot/wrapper-linux-$CPUARCH /agent/conf/wrapper.conf