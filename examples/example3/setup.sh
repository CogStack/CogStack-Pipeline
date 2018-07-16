#!/bin/bash
set -e


COMMON_DIR="../docker-common"
COMMON_OUT_DIR="docker/__common"

COGSTACK_OUT_DIR="docker/__cogstack"


# copy the relevant common data
#
echo "Copying the configuration files for the common docker images"

if [ -e $COMMON_OUT_DIR ]; then
	rm -r $COMMON_OUT_DIR;
fi

mkdir $COMMON_OUT_DIR
cp -r $COMMON_DIR/* $COMMON_OUT_DIR/


# setup the common containers
#
if [ ! -e $COMMON_OUT_DIR/nginx/auth/.htpasswd ]; then
	echo "Generating user:password --> 'test:test' for nginx proxy"
	mkdir $COMMON_OUT_DIR/nginx/auth
	htpasswd -b -c ./nginx/auth/.htpasswd 'test' 'test'
fi


# setup cogstack
#
echo "Generating properties files for CogStack"
( cd cogstack && bash gen_config.sh )

if [ -e $COGSTACK_OUT_DIR ]; then
	rm -r $COGSTACK_OUT_DIR;
fi

mkdir $COGSTACK_OUT_DIR
cp cogstack/conf/*.properties $COGSTACK_OUT_DIR/

echo "Done."
