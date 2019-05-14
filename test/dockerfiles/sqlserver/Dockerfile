FROM mcr.microsoft.com/mssql/server:2017-latest-ubuntu

WORKDIR /usr/src/app

COPY entrypoint.sh /usr/src/app/

COPY create-repo.sh /usr/src/app/
COPY create_schema.sql /usr/src/app/
RUN chmod +x /usr/src/app/create-repo.sh

CMD /bin/bash ./entrypoint.sh
