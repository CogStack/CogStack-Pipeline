#!/bin/bash
set -e


# global variables
#
export LC_ALL=C

DB_USER='test'
DB_NAME='db_samples'
DB_DUMP_FILE="db_samples-mt.sql.gz"

POSTGRES_USER=postgres

# HINT: this can be provided as an input parameter
DATA_DIR="../../rawdata"
IN_DATA='mtsamples-txt-full.tgz'


# entry point
#
if [ ! -e $DATA_DIR/$IN_DATA ]; then echo "Missing input data: $DATA_DIR/$IN_DATA" && exit 1; fi


# create the user, the database and set up the access
#
echo "Creating database: $DB_NAME and user: $DB_USER"
psql -v ON_ERROR_STOP=1 -U $POSTGRES_USER <<-EOSQL
CREATE DATABASE $DB_NAME;
CREATE ROLE $DB_USER WITH PASSWORD 'test' LOGIN;
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO test;
EOSQL


# create schemas
#
echo "Defining DB schemas"
psql -v ON_ERROR_STOP=1 -U $DB_USER -d $DB_NAME -f db_create_mt_schema.sql


# helper functions
#
parse_sql_insert_query () {
	# 1. parse the structural fields
	#
	# grep -m 1 # exits on Alpine, code: 141
	type=$( cat $1 | grep 'Sample Type / Medical Specialty:' | sed -e 's/Sample Type \/ Medical Specialty://' -e 's/^[[:space:]]*//g')
	name=$( cat $1 | grep 'Sample Name:' | sed -e 's/Sample Name://' -e 's/^[[:space:]]*//g' | sed -e "s/'/\\\'/g")
	desc=$( cat $1 | grep 'Description:' | sed -e 's/Description://' -e 's/^[[:space:]]*//g' | sed -e "s/'/\\\'/g")

	s_id=$(echo $1 | cut -d '-' -f 5 | cut -d '.' -f 1)
	type_id=$(echo $1 | cut -d '-' -f 3)

	# 2. parse the document field
	#
	doc=$( cat $1 | awk 'BEGIN { RS="----"; } /^-/ { print RS $0; }' | sed -e "s/-----//" | sed -e "s/'/\\\'/g")

	echo "INSERT INTO \
			samples(SAMPLE_ID, TYPE, TYPE_ID, NAME, DESCRIPTION, DOCUMENT) \
			VALUES($s_id, E'$type', $type_id, E'$name', E'$desc', E'$doc');"
}


# decompress the sample data
#
TMP_DIR=__tmp
if [ ! -e $TMP_DIR ]; then
	mkdir $TMP_DIR
fi

echo "Decompressing sample data"
tar -xzf $DATA_DIR/$IN_DATA -C $TMP_DIR


# parse the data and load into DB
#
SQL_FILE=__insert.sql
if [ -e $SQL_FILE ]; then rm $SQL_FILE; fi

echo "Performing 2-step bulk insert of the records into DB"
echo "-- 1) parsing mtsamples..."

for f in $TMP_DIR/mtsamples-type-*; do
	query="$( parse_sql_insert_query $f )"		# WARN: be use " as the returning
	echo "$query" >> $SQL_FILE					# string contains newlines !
done

echo "-- 2) inserting records into DB in bulk"
psql -v ON_ERROR_STOP=1 -U $DB_USER -d $DB_NAME -f $SQL_FILE > /dev/null


# perform a DB dump
#
echo "Done with initializing the sample data"
echo "Dumping the $DB_NAME DB into a compressed file $DB_DUMP_FILE"
pg_dump -U $DB_USER -d $DB_NAME -Z 9 > "$DB_DUMP_FILE"


# cleanup
#
echo "Cleaning up"
rm $SQL_FILE
rm -r $TMP_DIR
psql -v ON_ERROR_STOP=1 -U $POSTGRES_USER -c "DROP DATABASE $DB_NAME"
psql -v ON_ERROR_STOP=1 -U $POSTGRES_USER -c "DROP ROLE $DB_USER;"

echo "Done."
