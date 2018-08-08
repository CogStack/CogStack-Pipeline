#!/bin/bash
set -e

TAG=local

( cd docker-cogstack/java ; docker build -t cogstacksystems/cogstack-java-run:$TAG . )

#( cd docker-cogstack/fluentd ; docker build -t cogstacksystems/fluentd:$TAG . )

docker build -t cogstacksystems/cogstack-java-devel:$TAG -f Dockerfile.devel .

docker build -t cogstacksystems/cogstack-pipeline:$TAG -f Dockerfile.run .
