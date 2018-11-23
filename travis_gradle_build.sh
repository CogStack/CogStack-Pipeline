#!/bin/bash
# Abort on Error
set -e

export PING_SLEEP=30s
export WORKDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export BUILD_OUTPUT=$WORKDIR/build.out

DUMP_LINES=500

touch $BUILD_OUTPUT

dump_output() {
   echo Tailing the last $DUMP_LINES lines of output:
   tail -$DUMP_LINES $BUILD_OUTPUT
}
error_handler() {
  echo ERROR: An error was encountered with the build.
  dump_output
  exit 1
}
# If an error occurs, run our error handler to output a tail of the build
trap 'error_handler' ERR

# Set up a repeating loop to send some output to Travis.
bash -c "while true; do echo \$(date) - building ...; sleep $PING_SLEEP; done" &
PING_LOOP_PID=$!

# Build Commands
./gradlew -PdockerComposePath="$HOME/bin/docker-compose" acceptTest >> $BUILD_OUTPUT 2>&1
./gradlew -PdockerComposePath="$HOME/bin/docker-compose" postgresIntegTest >> $BUILD_OUTPUT 2>&1

# The build finished without returning an error so dump a tail of the output
dump_output

# nicely terminate the ping output loop
kill $PING_LOOP_PID
