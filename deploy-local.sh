#!/usr/bin/env bash

# Name to give the docker network and stack. Update network instances in docker-compose.yml file if modified.
name="cdr_local-minio"

function wait_for_containers () {
    waiting=$(docker service ls | grep ""$name"_.*0/1 ")
    printf "\nWaiting for docker services to start."
    while [ ! -z "$waiting" ]; do
        sleep 2
        printf "."
        waiting=$(docker service ls | grep ""$name"_.*0/1 ")
    done
    printf "\nDone!\n"
}

# Deploy docker-compose.yml file via docker stacks
function deploy_images () {
    printf "Deploying Docker Images...\n"
    echo $(pwd)
    docker stack deploy -c docker-compose.yml -c docker-compose-override-local.yml $name
}

# Create attachable overlay docker network
function create_network () {
    printf "Creating docker '$name' network...\n"
    docker network create --driver=overlay --attachable $name
}

# Check is network already does not exist or is not attachable.
# Prompt to create if nonexistent or continue if network exists and is attachable.
function check_network_configuration () {
    printf "Checking Docker Network...\n"
    network=`docker network ls | grep "$name"`
    networktype=$(docker network inspect $name 2>/dev/null | grep -i "\"driver\": \"overlay\"")
    attachable=$(docker network inspect $name 2>/dev/null | grep -i "\"attachable\": true")
    if [ -z "$network" ]; then
        read -p "Network '$name' does not exist, create it now? (y/N) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            create_network
        else
            printf "Exiting...\n"
            exit 0
        fi
    elif [ -z "$networktype" ] || [ -z "$attachable" ]; then
        printf  "ERROR: The docker network '$name' exists, but is not an attachable overlay network. Please update/remove this docker network and re-run this script. The network can be removed manually with 'docker network rm $name'.\n"
        exit 1
    fi
}

# Warning message and beginning of script
printf "\
====== WARNING: TEST DEPLOYMENT ======\n\
This docker deployment will create a local minio server with default secret and key. Make sure this isn't being deployed into production.\n\n"

if

check_network_configuration
deploy_images
wait_for_containers
