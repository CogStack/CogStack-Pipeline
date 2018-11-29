#!/bin/bash
set -e

LOG_FILE=__prepare_examples.log

echo "Preparing DB dumps for examples"

if [ -e $LOG_FILE ]; then rm $LOG_FILE; fi

echo "Preparing: example 1 ..."
if [ ! -e example1/db_dump ]; then mkdir example1/db_dump; fi
( cd example1/extra; bash prepare_db.sh; mv db_samples.sql.gz ../db_dump/ ) >> $LOG_FILE

echo "Preparing: example 2 ..."
if [ ! -e example2/db_dump ]; then mkdir example2/db_dump; fi
( cd example2/extra; bash prepare_db.sh; mv db_samples.sql.gz ../db_dump/ ) >> $LOG_FILE

echo "Preparing: example 3 ..."
if [ ! -e example3/db_dump ]; then mkdir example3/db_dump; fi
( cd example3/extra; bash prepare_synsamples_db.sh; bash prepare_mtsamples_db.sh; mv db_samples-*.sql.gz ../db_dump/ ) >> $LOG_FILE

echo "Preparing: example 4 ..."
if [ ! -e example4/db_dump ]; then mkdir example4/db_dump; fi
( cd example4/extra; bash prepare_db.sh; mv db_samples-*.sql.gz ../db_dump/ ) >> $LOG_FILE

echo "See log file: $LOG_FILE for more information"

echo "Done."
