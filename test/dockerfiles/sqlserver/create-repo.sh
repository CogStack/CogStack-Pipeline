#!/bin/bash
set -e

if [ -z ${WAIT_FOR_READY+x} ]; then
	WAIT_FOR_READY=15
fi

echo "Awaiting $WAIT_FOR_READY s SQL server to get ready ..."
sleep ${WAIT_FOR_READY}s

echo 'Creating DB schema'
/opt/mssql-tools/bin/sqlcmd -U sa -P $SA_PASSWORD -d master -i /usr/src/app/create_schema.sql

echo 'Initialization finished'