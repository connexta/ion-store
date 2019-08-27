#!/usr/bin/env bash

# No failures allowed
set -e

SET_DOCKER_REG=""
SET_DOCKER_W=""

DOCKER_REG=${DOCKER_REGISTRY:-${SET_DOCKER_REG}}

# The name of the preface for the docker images
DOCKER_IMAGE_NAME="cnxta/ion-store"

# DOCKER_REGISTRY

# The stack you want the docker compose file named
STACK="store-stack"

# The path to the Docker Wrapper
# IMPORTANT!
# >>> The base gc-docker script has to be in the same location as this file

DOCKER_W=${DOCKER_WRAPPER:-${SET_DOCKER_W}}

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# Tests that variables are correct for the docker compose and docker wrapper

function checkVars() {
    if [ -z ${DOCKER_REG} ]; then
        echo "> export DOCKER_REGISTRY=ip:port"
        return 1
    elif [ ! -f docker-compose.yml ]; then
        echo "$0 needs to be ran in the same directory as a docker-compose.yml"
        return 1
    elif [ ! -f ${DOCKER_W} ]; then
        echo "\"${DOCKER_W}\" does not exist"
        return 1
    elif [ ! -x ${DOCKER_W} ]; then
        echo "${DOCKER_W} is not executable"
        return 1
    fi
    return 0
}

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

checkVars || exit 1

header "Tagging and pushing the docker image to ${DOCKER_REG}"
run "docker tag ${DOCKER_IMAGE_NAME} ${DOCKER_REG}/${DOCKER_IMAGE_NAME}"
run "docker push ${DOCKER_REG}/${DOCKER_IMAGE_NAME}"

header "Pulling the docker image on ${DOCKER_REG}"
run "${DOCKER_W} pull ${DOCKER_REG}/${DOCKER_IMAGE_NAME}"

header "Deploying the application on ${STACK}"

run "${DOCKER_W} stack rm ${STACK}"

echo " $ ${DOCKER_W} stack deploy -c <(REGISTRY=${DOCKER_REG} docker-compose -f docker-compose.yml config) ${STACK}"
${DOCKER_W} stack deploy -c <(REGISTRY=${DOCKER_REG} docker-compose -f docker-compose.yml config) ${STACK}

run "${DOCKER_W} stack services ${STACK}"