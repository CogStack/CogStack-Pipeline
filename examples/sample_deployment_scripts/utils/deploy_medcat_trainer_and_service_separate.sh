#!/usr/bin/env bash


echo "This script must be run with root privileges."

pip3 --user install https://s3-us-west-2.amazonaws.com/ai2-s2-scispacy/releases/v0.2.4/en_core_sci_md-0.2.4.tar.gz
python3 -m spacy download en_core_web_sm
pip3 --user install docker-compose gunicorn flask werkzeug wheel uwsgi setuptools cython cpython

mkdir /root/deployment
cd /root/deployment

rm -rf ./MedCATservice-master 
rm -rf ./MedCATtrainer-master 

wget https://github.com/CogStack/MedCATtrainer/archive/master.zip
mv ./master.zip ./medcat-trainer-master.zip
unzip -o ./medcat-trainer-master.zip 

wget https://github.com/CogStack/MedCATservice/archive/master.zip
mv ./master.zip ./medcat-service-master.zip
unzip -o ./medcat-service-master.zip 

pip3 install -r ./MedCATservice-master/medcat_service/requirements.txt

rm -rf /etc/MedCATservice
rm -rf /etc/MedCATtrainer

mv ./MedCATservice-master /etc/MedCATservice
mv ./MedCATtrainer-master /etc/MedCATtrainer

bash /etc/MedCATservice/download_medmen.sh

# get the public vocab and cdb provided from the MedCAT repo
wget https://s3-eu-west-1.amazonaws.com/zkcl/vocab.dat --directory=/etc/MedCATservice/models/
wget https://s3-eu-west-1.amazonaws.com/zkcl/cdb-medmen.dat -O /etc/MedCATservice/models/cdb.dat

# Change permissions to root user or whatever user will be running the service
chown -R root:root /etc/MedCATservice
chown -R root:root /etc/MedCATtrainer

chmod -R 755 /etc/MedCATservice
chmod -R 755 /etc/MedCATtrainer

# ======== 
yes | cp ./systemd_medcat.service /etc/systemd/system/systemd_medcat.service
chown root:root /etc/systemd/system/systemd_medcat.service
chmod 755 /etc/systemd/system/systemd_medcat.service 

# Allow execution of script
chmod +x /etc/MedCATservice/start-service-prod.sh

# Validate and start service (optional)
systemd-analyze verify systemd_medcat.service
systemctl disable systemd_medcat.service
systemctl stop systemd_medcat.service
systemctl enable systemd_medcat.service
systemctl start systemd_medcat.service
