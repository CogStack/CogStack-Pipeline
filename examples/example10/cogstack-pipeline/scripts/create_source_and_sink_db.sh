#!/usr/bin/env bash

# Note: this is the path to the folder inside the coresponding Docker container
DATA_DIR="/data"
DB_DUMP_FILE="db_samples.sql.gz"

# create the user, the database and set up the access
echo "Creating database: $POSTGRES_DATABANK_DB and user: $POSTGRES_USER"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL

CREATE DATABASE $POSTGRES_DATABANK_DB;
ALTER ROLE $POSTGRES_USER WITH PASSWORD '$POSTGRES_PASSWORD'; 
ALTER ROLE $POSTGRES_USER WITH LOGIN;
GRANT ALL PRIVILEGES ON DATABASE $POSTGRES_DATABANK_DB TO $POSTGRES_USER;

EOSQL

#psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DATABANK_DB"<<-EOSQL

#EOSQL

echo "Loading data from CSV files."

# Load data from csv files into the previously created tables.

# psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DATABANK_DB"<<-EOSQL
# 
# 	\copy TABLENAME from '/data/some_csv_file.csv' delimiter ',' csv header NULL ''; 
#  
# EOSQL

echo "Restoring DB from dump" 

# import sql.gz file
gunzip -c $DATA_DIR/$DB_DUMP_FILE | psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER --dbname $POSTGRES_DATABANK_DB

# import sql file
#psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DATABANK_DB" -a -f $DATA_DIR/$DB_DUMP_FILE

echo "Done initializing the data."
