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

DB_DUMP_FILE_SYN="$DB_DIR/db_samples-syn.sql.gz"
DB_DUMP_FILE_MT="$DB_DIR/db_samples-mt.sql.gz"


# main entry point
#
echo "Generating deployment scripts"
if [ -e $DEPLOY_DIR ]; then rm -rf $DEPLOY_DIR; fi
mkdir $DEPLOY_DIR


# copy the relevant common data
#
if [ ! -e "$DB_DUMP_FILE_SYN" ]; then echo "Missing DB dump file: $DB_DUMP_FILE_SYN" && exit 1; fi
if [ ! -e "$DB_DUMP_FILE_MT" ]; then echo "Missing DB dump file: $DB_DUMP_FILE_MT" && exit 1; fi

echo "Copying the DB dump"
mkdir $DB_OUT_DIR
cp -r $DB_DIR/* $DB_OUT_DIR/


# copy the relevant common data
#
echo "Copying the configuration files for the common docker images"
mkdir $COMMON_OUT_DIR
cp -r $COMMON_DIR/* $COMMON_OUT_DIR/

cp $DOCKER_DIR/*.yml $DEPLOY_DIR/


# setup the common containers
#
if [ ! -e $COMMON_OUT_DIR/nginx/auth/.htpasswd ]; then
	echo "Generating user:password --> 'test:test' for nginx proxy"
	mkdir $COMMON_OUT_DIR/nginx/auth
	htpasswd -b -c $COMMON_OUT_DIR/nginx/auth/.htpasswd 'test' 'test'
fi


# setup cogstack
#
echo "Generating properties files for CogStack"
( cd cogstack && bash gen_config.sh )
mkdir $COGSTACK_OUT_DIR
cp $COGSTACK_DIR/conf/*.properties $COGSTACK_OUT_DIR/

echo "Done."
