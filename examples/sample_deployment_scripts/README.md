Don't forget to execute chmod -R u+x ./path_to_this_folders_scripts*.sh otherwise you won't be executing much...

Quickstart steps:

copy repo files to /etc/cogstack_deployment/ or any other location

find /etc/cogstack_deployment/cogstack_pipeline -name "*.sh" -execdir chmod u+x {} +
find /etc/cogstack_deployment/utils -name "*.sh" -execdir chmod u+x {} +

chmod -R 755 /etc/cogstack_deployment
chown -R root:root /etc/cogstack_deployment


Don't forget to add root user to the docker group.


The ``` db_samples.sql.gz ``` belongs to example9.

## Folder hierarchy & description

data - where the DB data sits, including the models folder
security - app accounts, certificates for kibana, nginx etc

standalone-service : each service is separate and operates in host network mode

cogstack-pipeline : config files for all services stored in their own directory, along with the master docker-compose.yml file, cogstack DB scripts are located inthe scripts folder

utils : tools for processeing data (anonymisation scripts, doc conversions, installing docker etc)

## Jupyter-hub configuration

The password is set in ./cogstack_pipeline/jupyter-hub/config/jupyter_notebook_config.py 

Please check https://jupyter-notebook.readthedocs.io/en/stable/public_server.html#securing-a-notebook-server for additional configs.
