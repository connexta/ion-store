#!/usr/bin/env bash

# No failures allowed
set -e

# The name of the preface for the docker images
DOCKER_NAME="cnxta/cdr-"

# The IP For the place you're storing this
DEPLOY_IP=""

# The port used by docker
PORT=""

# The stack you want the docker compose file named
STACK="cdr-stack"

# The command to build docker images
BUILD_CMD="./gradlew clean build -x test"

# The path to the Docker Wrapper
DOCKER_W=""

GITHUB_PATCH=""

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# Tests that variables are correct for the docker compose and docker wrapper

if [ -z ${DEPLOY_IP} ] && [ -z ${PORT} ] && [ -z ${DOCKER_W} ]; then
    echo "Set the global variables at the top of $0"
    exit 1

elif [ ! -f docker-compose.yml ]; then
    echo "$0 needs to be ran in the same directory as a docker-compose.yml"
    exit 1
elif [ ! -f ${DOCKER_W} ]; then
    echo "\"${DOCKER_W}\" does not exist!"
    exit 1
fi

function run() {
    echo " $ $@"
    $@
}

function header() {
    echo ""
    echo "# # # # # # # # # # # # # #"
    echo " $1"
    echo ""
}

if [[ ! -z GITHUB_PATCH ]]; then
    run "git apply ${GITHUB_PATCH}"
fi

header "Building Docker Images"
run ${BUILD_CMD}

listOfImages=$(docker image ls | egrep "^${DOCKER_NAME}" | awk '{print $1,$2}' OFS=':')

if [[ -z ${listOfImages} ]]; then
    echo "docker image ls | egrep ^${DOCKER_NAME} returned 0 images"
    exit 1
fi

header "Tagging and pushing docker images to ${DEPLOY_IP}:${PORT}"
for i in $(echo ${listOfImages}); do
    run "docker tag ${i} ${DEPLOY_IP}:${PORT}/${i}"
    run "docker push ${DEPLOY_IP}:${PORT}/${i}"
done

header "Pulling and tagging docker images on ${DEPLOY_IP}:${PORT}"

for i in $(echo ${listOfImages}); do
    run "${DOCKER_W} pull ${DEPLOY_IP}:${PORT}/${i}"
    run "${DOCKER_W} tag ${DEPLOY_IP}:${PORT}/${i} ${i}"
done

header "Deploying the application on ${STACK}"

run "${DOCKER_W} stack rm ${STACK}"

run "${DOCKER_W} stack deploy -c docker-compose.yml ${STACK}"

run "${DOCKER_W} stack services ${STACK}"