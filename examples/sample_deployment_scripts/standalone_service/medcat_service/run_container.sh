#!/usr/bin/env bash 
docker run --interactive --tty --name redhat_test_container --detach redhat_latest

docker run -it --restart=always  -v $(pwd)/models/:/cat/models:ro -p 5000:5000 --net=host --env-file=$(pwd)/envs/env_app --env-file=$(pwd)/envs/env_medcat cogstacksystems/medcat-service:latest
docker run -it --restart=always  -v $(pwd)/models/:/cat/models:ro -p 5000:5000 --net=host --env-file=$(pwd)/envs/env_app --env-file=$(pwd)/envs/env_medcat medcat-service:latest

# docker run -it --restart=always  -v $(pwd)/models:/cat/models:ro -p 5000:5000 --net=host --env-file=$(pwd)/envs/env_app --env-file=$(pwd)/envs/env_medcat medcat-service:latest