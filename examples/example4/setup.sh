#!/bin/bash
set -e


DEPLOY_DIR="__deploy"
TMP_DIR="$DEPLOY_DIR/__tmp"

COMMON_DIR="../docker-common"

COMMON_OUT_DIR="common"
COGSTACK_OUT_DIR="cogstack"


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


#if [ ! -e $COMMON_OUT_DIR/nginx/auth/.htpasswd ]; then
#	echo "Generating user:password --> 'test:test' for nginx proxy"
#	mkdir $COMMON_OUT_DIR/nginx/auth
#	htpasswd -b -c $COMMON_OUT_DIR/nginx/auth/.htpasswd 'test' 'test'
#fi

doc_types=(
	pdf-text
	pdf-img
	docx
	jpg)

for dt in ${doc_types[@]}; do
	echo "Generating use-case: ${dt}"
	dp="$DEPLOY_DIR/${dt}"

	if [ -e "${dp}" ]; then rm -r "${dp}"; fi
	mkdir "${dp}"

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

	# copy database dump
	#
	echo "-- copying db dump file"
	mkdir "${dp}/db_dump"
	cp "db_dump/db_samples-${dt}.sql.gz" "${dp}/db_dump/db_samples.sql.gz"

done


# cleanup
#
rm -rf $TMP_DIR


echo "Done."
