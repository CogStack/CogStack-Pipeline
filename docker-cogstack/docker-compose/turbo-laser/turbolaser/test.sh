#!/bin/bash

./wait-for-it.sh "biolark:5555" "--timeout=0"
./wait-for-it.sh "postgres:5432" "--timeout=0"
./wait-for-it.sh "activemq:61616" "--timeout=0"
./wait-for-it.sh "lb:80" "--timeout=0"


java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar /usr/src/turbolaser/turbo-laser-0.3.0.jar /usr/src/turbolaser_conf

