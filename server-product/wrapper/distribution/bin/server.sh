#!/bin/bash
# Java service launcher. JVM and service settings belong in conf/wrapper.conf.
set -e

#RUN_AS_USER=
FILES_TO_SOURCE=

# Resolve installed init-script symlinks before locating the distribution.
SCRIPT=${BASH_SOURCE[0]}
while [ -L "$SCRIPT" ]; do
    DIRECTORY=$(cd -P "$(dirname "$SCRIPT")" && pwd)
    SCRIPT=$(readlink "$SCRIPT")
    [[ "$SCRIPT" = /* ]] || SCRIPT=$DIRECTORY/$SCRIPT
done
DIRECTORY=$(cd -P "$(dirname "$SCRIPT")" && pwd)
SCRIPT=$DIRECTORY/$(basename "$SCRIPT")
cd "$DIRECTORY"
WRAPPER_CONF=../conf/wrapper.conf

if [ -f "${SCRIPT%.sh}.shconf" ]; then
    source "${SCRIPT%.sh}.shconf"
fi
if [ -n "$FILES_TO_SOURCE" ]; then
    IFS=';' read -r -a SOURCE_FILES <<< "$FILES_TO_SOURCE"
    for SOURCE_FILE in "${SOURCE_FILES[@]}"; do
        source "$SOURCE_FILE"
    done
fi
export RUN_AS_USER=${RUN_AS_USER:-}
export WRAPPER_SCRIPT=$SCRIPT

case "$(uname -s):$(uname -m)" in
    Linux:x86_64) BINARY=wrapper-linux-x86-64 ;;
    Linux:aarch64|Linux:arm64) BINARY=wrapper-linux-arm-64 ;;
    Darwin:arm64) BINARY=wrapper-macosx-arm-64 ;;
    Darwin:x86_64)
        if [ "$(sysctl -in sysctl.proc_translated 2>/dev/null || true)" = 1 ]; then
            BINARY=wrapper-macosx-arm-64
        else
            echo 'Wrapper requires Apple Silicon on macOS.' >&2; exit 1
        fi ;;
    MINGW*:*|MSYS*:*|CYGWIN*:*)
        case "${PROCESSOR_ARCHITEW6432:-${PROCESSOR_ARCHITECTURE:-}}" in
            ARM64|arm64) BINARY=wrapper-windows-arm-64.exe ;;
            AMD64|amd64) BINARY=wrapper-windows-x86-64.exe ;;
            *) echo 'Wrapper requires Windows amd64 or arm64.' >&2; exit 1 ;;
        esac ;;
    *) echo "Unsupported platform: $(uname -s) $(uname -m)" >&2; exit 1 ;;
esac
exec "$DIRECTORY/../boot/$BINARY" --script "$(basename "$SCRIPT" .sh)" "$WRAPPER_CONF" "$@"
