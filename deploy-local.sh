#!/usr/bin/env bash

#set -x
#trap read debug

# Name to give the docker network and stack. Update network instances in docker-compose.yml file if modified.
name=cdr

# Evaluate the first parameter passed to the function every 2 seconds.
# When the expression evaluates to an empty string, return.
function wait_for_empty_results() {
    while [[ ! -z  $(eval $1) ]]; do
        sleep 2
        echo $(eval $1)
        printf "."
    done
}

# Secrets and configs are immutable. Docker swarm reports error "only updates to Labels are allowed"
# when attempting to modify their values. Remove any existing configs and secrets before
# attempting to create new ones.
function clean () {
    # Removing the stack removes its configs and secrets and that allows their values to be changed (recreated)
    # Ignore err message if stack does not exist
    docker stack rm $name 2> /dev/null
}

function recreate_network () {
    # Remove and recreate network in case it has the wrong options
    docker network rm $name 2&> /dev/null
    if [ $? -eq 0 ]
       then
          # RC 0 means there was a network to remove. Wait for it for it to happen.
          echo "Waiting to remove network $name"
          wait_for_empty_results "docker network ls | grep -w $name"
   fi

  docker network ls
    # Once network is gone, recreate it
    docker network create --driver=overlay --attachable $name > /dev/null
    echo "----------------------"
      docker network ls


    exit
}

# Deploy docker-compose.yml file via docker stacks
function deploy_images () {
    docker stack deploy -c docker-compose.yml -c docker-compose-override-local.yml $name
}

function wait_for_services () {
#set -x
#trap read debug
    printf "Waiting for docker services to start"
    wait_for_empty_results "docker service ls | grep $name_.*0/[[:digit:]]"\
    echo "Services started"
}

# ----- BEGIN -----
clean
recreate_network
deploy_images
wait_for_services
echo Exiting
# ------ END ------
