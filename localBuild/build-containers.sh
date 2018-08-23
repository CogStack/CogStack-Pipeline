#!/bin/bash
set -e

VERSION_TAG=local

echo "--------------------------------"
echo "building image: cogstacksystems/cogstack-java-run:$VERSION_TAG"
( cd ../docker-cogstack/java ; docker build -t cogstacksystems/cogstack-java-run:$VERSION_TAG . )
echo ""

echo "--------------------------------"
echo "building image: cogstacksystems/fluentd:$VERSION_TAG"
( cd ../docker-cogstack/fluentd ; docker build -t cogstacksystems/fluentd:$VERSION_TAG . )
echo ""

echo "--------------------------------"
echo "building image: cogstacksystems/cogstack-java-devel:$VERSION_TAG"
( docker build --build-arg VERSION=$VERSION_TAG  -t cogstacksystems/cogstack-java-devel:$VERSION_TAG -f Dockerfile.devel .. )
echo ""

echo "--------------------------------"
echo "building image: cogstacksystems/cogstack-pipeline:$VERSION_TAG"
( docker build --build-arg VERSION=$VERSION_TAG -t cogstacksystems/cogstack-pipeline:$VERSION_TAG -f Dockerfile.run .. )
