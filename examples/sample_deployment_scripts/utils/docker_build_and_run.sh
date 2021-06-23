#!/usr/bin/env bash
IMAGE_VERSION="latest"

DOCKER_BASE_IMAGE=$PWD"/base_debian_image.dockerfile"

docker build -t base_image_debian:${IMAGE_VERSION} . -f $DOCKER_BASE_IMAGE --force-rm
docker build -t cogstack:${IMAGE_VERSION} . -f $PWD"/cogstack.dockerfile" --force-rm

docker image prune --force

# Run the images by creating containers
# docker run --name cogstack -d -it cogstack 


# docker-compose up --renew-anon-volumes