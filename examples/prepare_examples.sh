#!/bin/bash

set -e

LOG_FILE=__prepare_examples.log

echo "Preparing examples data for deployment"

if [ -e $LOG_FILE ]; then
	rm $LOG_FILE
fi

echo "Preparing example 1 ..."
( cd example1/extra; bash prepare_db.sh ; mv db_samples.sql.gz ../docker/db_dump/ ) >> $LOG_FILE

echo "Preparing example 2 ..."
( cd example2/extra; bash prepare_db.sh ; mv db_samples.sql.gz ../docker/db_dump/ ) >> $LOG_FILE

echo "Preparing example 3 ..."
( cd example3/extra; bash prepare_synsamples_db.sh ; mv db_synsamples.sql.gz ../docker/db_dump/syn/db_samples.sql.gz ) >> $LOG_FILE
( cd example3/extra; bash prepare_mtsamples_db.sh ; mv db_mtsamples.sql.gz ../docker/db_dump/mt/db_samples.sql.gz ) >> $LOG_FILE

echo "See log file: $LOG_FILE for more information"

echo "Done."
