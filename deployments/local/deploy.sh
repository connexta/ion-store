#!/usr/bin/env bash

# Name to give the docker network and stack.

# Warning: $name is used for both the stack name and network name. The network name is hard-coded
# in the compose-override file  in this directory
# because variable substitution would append an underscore to $name in
# in the compose-override file.
# The consequence is that If the value of name is modified here, the compose-override file
# should be edited and the string "cdr" replaced with the new value of name.
name=cdr

# Evaluate the first parameter passed to the function every periodically.
# When the expression evaluates to an empty string, return.
function wait_for_empty_results() {
  sleep 1
  while [[ -n  $(eval $1) ]]; do
    sleep 1
    printf "." >&2
  done
}

# Secrets and configs are immutable. Docker swarm reports error "only updates to Labels are allowed"
# when attempting to modify their values. Remove any existing configs and secrets before
# attempting to create new ones.
function remove_stack () {
  # Removing the stack removes its configs and secrets and that allows their values to be changed (recreated)
  # Ignore err message if stack does not exist
  docker stack rm $name 2&> /dev/null
}


# Attachable networks cannot be created in compose files. Create the network in this script.
function recreate_network () {
  docker network rm $name 2&> /dev/null
  printf "Recreating network $name"

  # Match only on the exact name (-w for whole word)
  wait_for_empty_results "docker network ls | grep -w $name"
  printf "\n"

  # Once network is gone, create it
  docker network create --driver=overlay --attachable $name > /dev/null
}

# Deploy docker-compose.yml file via docker stacks
function deploy_stack () {
  docker stack deploy -c ../../docker-compose.yml -c docker-compose-override-local.yml $name
}

function wait_for_services () {
  printf "Starting services"
  wait_for_empty_results "docker service ls | grep $name_.*0/[[:digit:]]"
  printf "\n"
}

# ----- BEGIN -----
remove_stack
recreate_network
deploy_stack
wait_for_services
echo "Done"
# ------ END ------
