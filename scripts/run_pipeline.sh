#!/bin/bash

# wait for the services to get ready
#
echo "*** Awaiting services to start CogStack Pipeline ***"

while IFS=',' read -ra ADDR; do
  for i in "${ADDR[@]}"; do
      ./wait_for_service.sh "$i" "--timeout=0" "--stdout"
  done
done <<< $SERVICES_USED


# run the pipeline
#
echo "*** Starting CogStack Pipeline ***"

java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar ${1} ${2}
