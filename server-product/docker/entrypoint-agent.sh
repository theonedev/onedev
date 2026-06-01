#!/bin/bash

set -e 

UNAME=`uname -m`

if [[ $UNAME == "aarch64" ]]; then
    CPUARCH="arm-64"
else
    CPUARCH="x86-64"
fi

cd /agent/boot
exec ./wrapper-linux-$CPUARCH /agent/conf/wrapper.conf
