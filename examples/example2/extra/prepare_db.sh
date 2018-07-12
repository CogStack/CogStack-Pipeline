#!/bin/bash
set -e


# global variables
#
DB_USER='test'
DB_NAME='db_samples'
DB_DUMP_FILE="db_samples.sql.gz"

POSTGRES_USER=postgres

# HINT: this can be provided as an input parameter
DATA_DIR="../../rawdata"
IN_SYN_DATA='synsamples.tgz'
IN_MT_DATA='mtsamples.tgz'


# create the user, the database and set up the access
#
echo "Creating database: $DB_NAME and user: $DB_USER"
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" <<-EOSQL
CREATE DATABASE $DB_NAME;
CREATE ROLE $DB_USER WITH PASSWORD 'test' LOGIN;
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO test;
EOSQL

export PGPASWORD='postgres'


# create schemas
#
echo "Defining DB schemas"
psql -v ON_ERROR_STOP=1 -U $DB_USER -d $DB_NAME -f db_create_schema.sql


# decompress the sample data
#
TMP_DIR=__tmp
if [ ! -e $TMP_DIR ]; then
	mkdir $TMP_DIR
fi

echo "Decompressing sample data"
tar -xzf $DATA_DIR/$IN_SYN_DATA -C $TMP_DIR
tar -xzf $DATA_DIR/$IN_MT_DATA -C $TMP_DIR


# load data -- the secure option
#
echo "2-step loading data into DB"
csv_files=(
	patients
	encounters
	observations)

echo "-- loading synthetic data"
for f in ${csv_files[@]}; do
	echo "----> Loading table: ${f}"
	header=$(head -n 1 "$TMP_DIR/${f}.csv")
	tail -n +2 "$TMP_DIR/${f}.csv" | psql -v ON_ERROR_STOP=1 -U $DB_USER -d $DB_NAME -c "COPY ${f}($header) FROM STDIN DELIMITER ',' CSV"
done

echo "-- loading mt samples data"
NF=$( ls -1q $TMP_DIR/mtsamples-type-* | wc -l )
i=1
for f in $TMP_DIR/mtsamples-type-*; do
	doc=$(cat $f | sed -e "s/'/\\\'/g")
	psql -v ON_ERROR_STOP=1 -U $DB_USER -d $DB_NAME -c "UPDATE encounters SET document = E'$doc' WHERE cid = $i;" > /dev/null

	let i=i+77
	if [ "$i" -gt "$NF" ]; then let i=i%NF+1; fi
done


# perform a DB dump
#
echo "Done with initializing the sample data"
echo "Dumping the $DB_NAME DB into a compressed file $DB_DUMP_FILE"
pg_dump -U $DB_USER -d $DB_NAME -Z 9 > "$DB_DUMP_FILE"


# cleanup
#
echo "Cleaning up"
rm -r $TMP_DIR
psql -v ON_ERROR_STOP=1 -U $POSTGRES_USER -c "DROP DATABASE $DB_NAME"
psql -v ON_ERROR_STOP=1 -U $POSTGRES_USER -c "DROP ROLE $DB_USER;"

echo "Done."
