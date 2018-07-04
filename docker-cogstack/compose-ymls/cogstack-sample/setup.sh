#!/bin/bash
set -e

# basic setup script
#
echo "Genearating properties files for CogStack"
( cd cogstack && bash gen_config.sh )
 
if [ ! -e ./nginx/auth/.htpasswd ]; then
	echo "Generating user:password --> 'test:test' for nginx proxy"
	htpasswd -b -c ./nginx/auth/.htpasswd 'test' 'test'
fi

echo "Decompressing the raw data"
( cd rawdata && tar -xvf sample.tgz )

echo "Done."