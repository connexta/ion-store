#!/usr/bin/env bash

networkName="cdr"
stackName="store-stack"

helptext="\
Usage: deploy.sh [command]
Utility script for setting up a docker network and deploying a local instance of CDR into a docker swarm.
'update' is the default option if no command is given.

Commands:
  u, update\t\tUpdate deployed stack. Fastest option, but does not pick up all changes. Create network if necessary. Exit with error if the network exsists, but is not properly configured.
  r, redeploy\t\tStop existing stack. Picks up all changes except changes to the overlay network. Create the docker network (if necessary) and deploy stack. Exit with error if the network exsists, but is not properly configured.
  c, clean\t\tRemove the deployed stack and network.
"

# Evaluate the first parameter passed to the function every periodically.
# When the expression evaluates to an empty string, return.
function wait-for-empty-results() {
  sleep 1
  while [[ -n  $(eval $1) ]]; do
    sleep 1
    printf "." >&2
  done
}

# Stop running stack
function rm-stack () {
    printf "Removing $stackName docker stack...\n"
    docker stack rm $stackName
    printf "Done!\n"
}

# Docker networks usually take a few seconds to go down, so it waits to prevent redeployment issues
function rm-network () {
    printf "Removing $networkName docker network...\n"
    docker network rm $networkName
    printf "\nWaiting for docker network '$networkName' to go down.\n"
    wait-for-empty-results "docker network ls | grep -w $networkName"
    printf "\nDone!\n"
}

# Waits for everything to start up
function wait-for-containers () {
    printf "\nWaiting for docker services to start (safe to exit)."
    wait-for-empty-results "docker service ls | grep $stackName_*0/[1-9]"
    printf "Done!\n"
}

# Deploy docker-compose.yml file via docker stacks
function deploy-images () {
    printf "\nDeploying Docker Images...\n"
    docker stack deploy -c ../../docker-compose.yml -c docker-override.yml $stackName
}

# Create attachable overlay docker network
function create-network () {
    printf "\nCreating docker '$networkName' network...\n"
    docker network create --driver=overlay --attachable $networkName
}

# Check is network already does not exist or is not attachable.
function configure-network () {
    printf "Checking Docker Network...\n"
    network=`docker network ls | grep "$networkName"`
    networktype=$(docker network inspect $networkName 2>/dev/null | grep -i "\"driver\": \"overlay\"")
    attachable=$(docker network inspect $networkName 2>/dev/null | grep -i "\"attachable\": true")
    if [ -z "$network" ]; then
        printf "Network '$networkName' does not exist, creating new network... \n"
        create-network
    elif [ -z "$networktype" ] || [ -z "$attachable" ]; then
        printf  "ERROR: The docker network '$networkName' exists, but is not an attachable overlay network. Please run 'clean' or 'redeploy' to remove or update this network.\n"
        exit 1
    else
        printf "Network '$networkName' already exists and is attachable. \n"
    fi
}

# Picks up changes to Dockerfiles and Compose files
function redeploy () {
    print_warning
    rm-stack
    configure-network
    deploy-images
    wait-for-containers
}

# Fastest option. Does not pick up all changes.
function update () {
    print_warning
    configure-network
    deploy-images
    wait-for-containers
}

function print_warning () {
    printf "====== WARNING: TEST DEPLOYMENT ======\nThis docker deployment will create a local minio server with default secret and key. Make sure this isn't being deployed into production.\n\n"
}

# start of script
if [ -z $1 ]; then
    update
else
   case $1 in
    update | u)
        update
        ;;
    redeploy | r)
        redeploy
        ;;
    clean | c)
        rm-stack
        rm-network
        ;;
    *)
        printf "$helptext"
        ;;
    esac
fi
