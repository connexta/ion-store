#!/usr/bin/env bash

# Name to give the docker network and stack. Update network instances in docker-compose.yml file if modified.
name="cdr"

helptext="\
Usage: deploy.sh [command]
Utility script for setting up a docker network and deploying a local instance of CDR into a docker swarm.
'deploy' is the default option if no command is given.

Commands:
  d, deploy\t\tcreate the docker network and stack, stopping if one already exists
  u, update\t\tattempts an in-place update of the deployed docker stack
  c, clean\t\tremoves the deployed docker stack and network (destructive)
  r, redeploy\t\tclean + deploy: completely removes the stack and network and deploys new instances (destructive)
"

# Removes the docker stack and network.
# Docker networks usually take a few seconds to go down, so it waits to prevent redeployment issues
function cleanup () {
    printf "Removing $name docker stack and network...\n"
    docker stack rm $name
    docker network rm $name
    waiting=$(docker network ls | grep "$name")
    printf "\nWaiting for docker network '$name' to go down."
    while [ ! -z "$waiting" ]; do
        sleep 2
        printf "."
        waiting=$(docker network ls | grep " $name ")
    done
    printf "\nDone!\n"
}

# Waits for everything to start up
function wait_for_containers () {
    waiting=$(docker service ls | grep ""$name"_.*0/1 ")
    printf "\nWaiting for docker services to start (safe to exit)."
    while [ ! -z "$waiting" ]; do
        sleep 2
        printf "."
        waiting=$(docker service ls | grep ""$name"_.*0/1 ")
    done
    printf "\nDone!\n"
}

# Deploy docker-compose.yml file via docker stacks
function deploy_images () {
    printf "\nDeploying Docker Images...\n"
    exists=$(docker stack ls | grep "$name")
    if [ ! -z "$exists" ]; then
        printf "[ERROR] '$name' stack already exists. Please run script with 'clean', 'redeploy', or 'help' for more info.\n"
        exit 1
    fi
    docker stack deploy -c docker-compose.yml $name
}

# Create attachable overlay docker network
function create_new_network () {
    printf "Creating docker '$name' network...\n"
    docker network create --driver=overlay --attachable $name
}

# Check is network already does not exist or is not attachable.
function configure_network () {
    printf "Checking Docker Network...\n"
    network=`docker network ls | grep "$name"`
    networktype=$(docker network inspect $name 2>/dev/null | grep -i "\"driver\": \"overlay\"")
    attachable=$(docker network inspect $name 2>/dev/null | grep -i "\"attachable\": true")
    if [ -z "$network" ]; then
        printf "Network '$name' does not exist, creating new network... \n"
        create_new_network
    elif [ -z "$networktype" ] || [ -z "$attachable" ]; then
        printf  "ERROR: The docker network '$name' exists, but is not an attachable overlay network. Please run 'clean' or 'redeploy' to remove or update this network.\n"
        exit 1
    else
        printf "Network '$name' already exists and is attachable. \n"
    fi
}

function print_warning () {
    printf "\
====== WARNING: TEST DEPLOYMENT ======\n\
This docker deployment will create a local minio server with default secret and key. Make sure this isn't being deployed into production.\n\n"
}

# start of script
if [ -z $1 ]; then
    print_warning
    configure_network
    deploy_images
    wait_for_containers
else
   case $1 in
    deploy | d)
        print_warning
        configure_network
        deploy_images
        wait_for_containers
        ;;
    clean | c)
        cleanup
        ;;
    update | u)
        deploy_images
        ;;
    redeploy | r)
        print_warning
        cleanup
        create_new_network
        deploy_images
        wait_for_containers
        ;;
    *)
        printf "$helptext"
        ;;
    esac
fi
