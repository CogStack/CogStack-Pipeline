#!/bin/bash
set -e


DEPLOY_DIR="__deploy"

COMMON_DIR="../docker-common"

COMMON_OUT_DIR="common"
COGSTACK_OUT_DIR="cogstack"
DB_DIR="db_dump"


# main entry point
#
echo "Generating deployment scripts"
if [ -e $DEPLOY_DIR ]; then rm -rf $DEPLOY_DIR; fi
mkdir $DEPLOY_DIR


# used services
#
services=(postgres
	pgsamples
	elasticsearch
	kibana)

# document-type use-cases
#
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
	echo "-- copying the configuration files for the common docker images and setting up services"

	if [ -e "${dp}/$COMMON_OUT_DIR" ]; then rm -r "${dp}/$COMMON_OUT_DIR"; fi
	mkdir "${dp}/$COMMON_OUT_DIR"

	for sv in ${services[@]}; do
		echo "---- setting up: ${sv}" 
		cp -r $COMMON_DIR/${sv} "${dp}/$COMMON_OUT_DIR/"
	done

	# setup cogstack
	#
	echo "-- copying CogStack files"
	mkdir "${dp}/$COGSTACK_OUT_DIR"

	cp cogstack/*.properties "${dp}/$COGSTACK_OUT_DIR/"

	# copy docker files
	#
	echo "-- copying docker-compose file"
	cp docker/docker-compose.override.yml "${dp}/"
	cp $COMMON_DIR/docker-compose.yml "${dp}/"
done


echo "Done."
