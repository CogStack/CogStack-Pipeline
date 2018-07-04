#!/bin/bash
set -e


# global variables
#
DB_USER='test'
DB_NAME='db_sample'


# create the user, the database and set up the access
#
echo "Creating database: $DB_NAME and user: $DB_USER"
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" <<-EOSQL
CREATE DATABASE $DB_NAME;
CREATE ROLE $DB_USER WITH PASSWORD 'test' LOGIN;
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO test;
EOSQL

export PGPASWORD='test'


# create schemas
#
echo "Defining DB schemas"
psql -v ON_ERROR_STOP=1 -U $DB_USER -d $DB_NAME -f /conf/db_create_schema.sql


# load data -- the secure option
#
echo "Loading data into DB"
csv_files=(
	patients
	encounters
	observations
	procedures
	allergies
	careplans
	conditions
	imaging_studies
	immunizations
	medications
	)

for f in ${csv_files[@]}; do
	echo "-- Loading table: ${f}"
	header=$(head -n 1 "/data/${f}.csv")
	tail -n +2 "/data/${f}.csv" | psql -v ON_ERROR_STOP=1 -U $DB_USER -d $DB_NAME -c "COPY ${f}($header) FROM STDIN DELIMITER ',' CSV"
done



# cleanup
#
echo "Done with initializing the sample data."


#psql -v ON_ERROR_STOP=1 -U test -d sample_db -f psql_load_data.sql
#psql -v ON_ERROR_STOP=1 --username "test" -d sample_db <<-EOSQL
#COPY patients FROM '/data/patients.csv' DELIMITER ',' CSV HEADER;
#-- ...
#EOSQL
