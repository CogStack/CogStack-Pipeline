#!/bin/bash

./wait-for-it.sh "bioyodie:8080" "--timeout=0"
./wait-for-it.sh "postgres:5432" "--timeout=0"
./wait-for-it.sh "lb:80" "--timeout=0"


java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar /usr/src/cogstack-1.2.0.jar /usr/src/cogstack_conf