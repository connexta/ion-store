#!/usr/bin/env bash

# Name to give the docker network and stack. Update network instances in docker-compose.yml file if modified.
name="cdr-local"

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
    docker stack deploy -c docker-compose.yml -c docker-compose-override-local.yml $name
}

# Secrets and configs are immutable. Docker swarm reports error "only updates to Labels are allowed"
# when attempting to modify their values. Remove any existing configs and secrets before
# attempting to create new ones.
function clean () {
    # Docker swarm prevents configs and secrets from being removed
    # if they are being used by a service, so shut down the stack if it is running.
    running_stack=$(docker stack ls | grep -w $name)
    [ ! -z "$running_stack" ] && docker stack rm $name > /dev/null && echo "Removed stack: $name"

    configs=$(docker config ls  | grep $name | tr -s " " | cut -d" " -f2)
    [ ! -z "$configs" ] && docker config rm $configs && echo "Removed configs: $configs"

    secrets=$(docker secret ls | grep $name | tr -s " " | cut -d" " -f2)
    [ ! -z "$secrets" ] && docker config rm $secrets && echo "Removed secrets: $secrets"
}

function recreate_network () {
    # Remove network in case it has the wrong options
    docker network rm $name 2&> /dev/null
    docker network create --driver=overlay --attachable $name > /dev/null
}


# Begin script
clean
recreate_network
deploy_images
wait_for_containers
