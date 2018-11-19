#!/bin/bash

# wait for the services to get ready
#
echo "*** Awaiting services to start CogStack Pipeline ***"

while IFS=',' read -ra ADDR; do
  for i in "${ADDR[@]}"; do
      ./wait_for_service.sh "$i" "--timeout=0" "--stdout"
  done
done <<< $SERVICES_USED


# start cogstack pipeline
#
echo "*** Starting CogStack Pipeline ***"

cog_start=`date +%s`

COG_PATH=/cogstack
COG_CONFIG_PATH=$COG_PATH/cogstack_conf

time sh -c "java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar ${1} $COG_CONFIG_PATH/step-1/"
time sh -c "java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar ${1} $COG_CONFIG_PATH/step-2/"

cog_end=`date +%s`

echo "*** Finishing CogStack Pipeline ***"

runtime_s=$((cog_end-cog_start))
runtime_m=$((runtime_s/60))
runtime_h=$((runtime_m/60))

echo "Duration: $runtime_h h $((runtime_m-(runtime_h*60))) m $((runtime_s-(runtime_m*60))) s"
echo "Total: $runtime_s sec"
