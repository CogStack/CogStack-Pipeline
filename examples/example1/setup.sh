#!/bin/bash
set -e


DEPLOY_DIR="__deploy"

COMMON_DIR="../docker-common"
COGSTACK_DIR="./cogstack"
DB_DIR="./db_dump"
DOCKER_DIR="./docker"

COMMON_OUT_DIR="$DEPLOY_DIR/common"
COGSTACK_OUT_DIR="$DEPLOY_DIR/cogstack"
DB_OUT_DIR="$DEPLOY_DIR/db_dump"

DB_DUMP_FILE="$DB_DIR/db_samples.sql.gz"


# main entry point
#
echo "Generating deployment scripts"
if [ -e $DEPLOY_DIR ]; then rm -rf $DEPLOY_DIR; fi
mkdir $DEPLOY_DIR


# copy the relevant common data
#
if [ ! -e $DB_DUMP_FILE ]; then echo "Missing DB dump file: $DB_DUMP_FILE" && exit 1; fi

echo "Copying the DB dump"
mkdir $DB_OUT_DIR
cp $DB_DUMP_FILE $DB_OUT_DIR/


# copy the relevant configuration data for microservices
#
services=(pgjobrepo
	pgsamples
	elasticsearch
	kibana)

echo "Copying the configuration files for the common docker images and setting up services"
mkdir $COMMON_OUT_DIR

for sv in ${services[@]}; do
	echo "-- Setting up: ${sv}" 
	cp -r $COMMON_DIR/${sv} $COMMON_OUT_DIR/
done

cp $COMMON_DIR/docker-compose.yml $DEPLOY_DIR/
cp $DOCKER_DIR/docker-compose.override.yml $DEPLOY_DIR/


# setup cogstack
#
echo "Generating properties files for CogStack"
mkdir $COGSTACK_OUT_DIR
cp $COGSTACK_DIR/*.properties $COGSTACK_OUT_DIR/

echo "Done."
