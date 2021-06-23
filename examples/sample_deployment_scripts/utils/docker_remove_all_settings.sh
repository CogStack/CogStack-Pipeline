#!/usr/bin/env bash

echo "WARNING, irreversible commands, you will lose all data + settings on the docker containers ! "
echo -n "Do you wish to remove all docker images, volumes and containers (y/n)? "
read answer

if [ "$answer" != "${answer#[Yy]}" ] ;then
    docker container rm $(docker container list -a -q) --force
    #docker rmi $(docker image list -a -q) --force
    docker volume rm $(docker volume ls -q) --force
else
    echo No
fi