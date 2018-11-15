#!/bin/bash

echo "*** Awaiting to start CogStack service ***"

./wait-for-it.sh "postgres:5432" "--timeout=0"
./wait-for-it.sh "elasticsearch:9200" "--timeout=0"

echo "*** Starting CogStack service ***"

cog_start=`date +%s`

COG_PATH=/cogstack

#COG_CONFIG_PATH=$COG_PATH/conf
COG_CONFIG_PATH=$COG_PATH/cogstack_conf


#time sh -c "java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar ${1} ${2}"

#time sh -c "java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar ${1} /cogstack/conf/step-1/"
#time sh -c "java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar ${1} /cogstack/conf/step-2/"

time sh -c "java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar ${1} $COG_CONFIG_PATH/step-1/"
time sh -c "java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar ${1} $COG_CONFIG_PATH/step-2/"

cog_end=`date +%s`

echo "*** Finishing CogStack service ***"

runtime_s=$((cog_end-cog_start))
runtime_m=$((runtime_s/60))
runtime_h=$((runtime_m/60))

echo "Duration: $runtime_h h $((runtime_m-(runtime_h*60))) m $((runtime_s-(runtime_m*60))) s"
echo "Total: $runtime_s sec"

#TIME_TO_SLEEP_S=600
#echo "And now, going to sleep $TIME_TO_SLEEP_S s..."
#sleep $TIME_TO_SLEEP_S