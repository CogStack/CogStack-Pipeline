# start SQL Server, start the script to create the DB and import the data
# see: https://docs.microsoft.com/en-us/sql/linux/sql-server-linux-configure-docker
(bash /usr/src/app/create-repo.sh) & /opt/mssql/bin/sqlservr