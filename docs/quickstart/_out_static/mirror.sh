#!/bin/bash

set -e

OUT_DIR=_www

if [ -e $OUT_DIR ]; then
	rm -r $OUT_DIR
fi

mkdir $OUT_DIR

( cd $OUT_DIR ; wget -E -H -K -k -p http://localhost:4000/ )
