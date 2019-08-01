#!/usr/bin/env bash

# Name to give the docker network and stack. Update network instances in docker-compose.yml file if modified.
name="cdr"

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
    printf "Removing $name docker stack...\n"
    docker stack rm $name
    printf "Done!\n"
}

# Docker networks usually take a few seconds to go down, so it waits to prevent redeployment issues
function rm-network () {
    printf "Removing $name docker network...\n"
    docker network rm $name
    printf "\nWaiting for docker network '$name' to go down.\n"
    wait-for-empty-results "docker network ls | grep -w $name"
    printf "\nDone!\n"
}

# Waits for everything to start up
function wait-for-containers () {
    printf "\nWaiting for docker services to start (safe to exit)."
    wait-for-empty-results "docker service ls | grep $name_.*0/[1-9]"
    printf "Done!\n"
}

# Deploy docker-compose.yml file via docker stacks
function deploy-images () {
    printf "\nDeploying Docker Images...\n"
    docker stack deploy -c ../../docker-compose.yml -c docker-override.yml $name
}

# Create attachable overlay docker network
function create-network () {
    printf "\nCreating docker '$name' network...\n"
    docker network create --driver=overlay --attachable $name
}

# Check is network already does not exist or is not attachable.
function configure-network () {
    printf "Checking Docker Network...\n"
    network=`docker network ls | grep "$name"`
    networktype=$(docker network inspect $name 2>/dev/null | grep -i "\"driver\": \"overlay\"")
    attachable=$(docker network inspect $name 2>/dev/null | grep -i "\"attachable\": true")
    if [ -z "$network" ]; then
        printf "Network '$name' does not exist, creating new network... \n"
        create-network
    elif [ -z "$networktype" ] || [ -z "$attachable" ]; then
        printf  "ERROR: The docker network '$name' exists, but is not an attachable overlay network. Please run 'clean' or 'redeploy' to remove or update this network.\n"
        exit 1
    else
        printf "Network '$name' already exists and is attachable. \n"
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
