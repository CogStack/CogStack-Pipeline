#!/bin/bash
set -e


DEPLOY_DIR="__deploy"
TMP_DIR="$DEPLOY_DIR/__tmp"

COMMON_DIR="../docker-common"

COMMON_OUT_DIR="common"
COGSTACK_OUT_DIR="cogstack"
DB_DIR="db_dump"


# main entry point
#
echo "Generating deployment scripts"
if [ -e $DEPLOY_DIR ]; then rm -rf $DEPLOY_DIR; fi
mkdir $DEPLOY_DIR


# setup the common containers
#
echo "Generating user:password --> 'test:test' for nginx proxy"
mkdir $TMP_DIR
htpasswd -b -c "$TMP_DIR/.htpasswd" 'test' 'test'

doc_types=(docx
	pdf-text
	pdf-img
	jpg)

for dt in ${doc_types[@]}; do
	echo "Generating use-case: ${dt}"

	dp="$DEPLOY_DIR/${dt}"
	mkdir "${dp}"

	# copy database dump
	#
	echo "-- copying db dump file"
	DATA_SIZE="small"
	db_file="$DB_DIR/db_samples-${dt}-$DATA_SIZE.sql.gz"
	if [ ! -e $db_file ]; then
		echo "DB dump file: $db_file does not exist"
		exit 1
	fi
	mkdir "${dp}/$DB_DIR"
	cp $db_file "${dp}/$DB_DIR/db_samples.sql.gz"

	# copy the relevant common data
	#
	echo "-- copying the configuration files for the common docker images"

	if [ -e "${dp}/$COMMON_OUT_DIR" ]; then rm -r "${dp}/$COMMON_OUT_DIR"; fi
	mkdir "${dp}/$COMMON_OUT_DIR"

	cp -r $COMMON_DIR/* "${dp}/$COMMON_OUT_DIR"

	# copy the generated .htpasswd file
	#
	if [ ! -e "${dp}/$COMMON_OUT_DIR/nginx/auth" ]; then mkdir "${dp}/$COMMON_OUT_DIR/nginx/auth"; fi
	cp $TMP_DIR/.htpasswd "${dp}/$COMMON_OUT_DIR/nginx/auth/"

	# setup cogstack
	#
	echo "-- copying CogStack files"
	mkdir "${dp}/$COGSTACK_OUT_DIR"

	cp cogstack/*.properties "${dp}/$COGSTACK_OUT_DIR/"
	cp cogstack/test2.sh "${dp}/$COGSTACK_OUT_DIR/"

	# copy docker files
	#
	echo "-- copying docker-compose file"
	cp docker/docker-compose.yml "${dp}/"
done


# cleanup
#
rm -rf $TMP_DIR

echo "Done."
