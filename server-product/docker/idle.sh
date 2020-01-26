#!/bin/bash

_sighandler() {
  kill -TERM "$child"
  wait "$child"
  exit 0
}

trap _sighandler SIGINT SIGTERM

while [ true ]; do
  sleep 60 &
  child=$!
  wait "$child"
done
