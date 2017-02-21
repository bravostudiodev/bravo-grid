#!/bin/bash
#
# Run docker-compose in a container
#
# This script will attempt to mirror the host paths by using volumes for the
# following paths:
#   * $(pwd)
#   * $(dirname $COMPOSE_FILE) if it's set
#   * $HOME if it's set
#
# You can add additional volumes (or any docker run options) using
# the $COMPOSE_OPTIONS environment variable.
#
# This script is based on: https://github.com/docker/compose/releases/download/1.11.1/run.sh
# with better handling of command line options --volume-from and -v

set -e

VERSION="1.11.1"
IMAGE="docker/compose:$VERSION"

for cntId in $(docker ps -a -q); do
 cntHost=$(docker inspect -f '{{.Config.Hostname}}' ${cntId})
 #echo "${cntId} -> ${cntHost}"
 if [ "${cntHost}" == "${HOSTNAME}" ]; then
  IN_CONTAINER=${cntId}
 fi
done

# Setup options for connecting to docker host
if [ -z "$DOCKER_HOST" ]; then
  DOCKER_HOST="/var/run/docker.sock"
fi

#docker inspect -f '{{range .HostConfig.VolumesFrom }}{{ . }} {{end}}'  ${IN_CONTAINER} | sed -e 's/ $//g' -e 's/ /\n/g'
#docker inspect -f '{{range .Mounts}}{{if ne "local" .Driver}}{{.Destination}} {{end}}{{end}}' ${IN_CONTAINER} | sed -e 's/ $//g' -e 's/ /\n/g'
#docker inspect ${IN_CONTAINER} | jq '.[0].Mounts' | jq '.[] | select(.Name!=null) | { VOLUME_FROM: .Name }' | grep : | cut -d '"' -f 4

if [ ! -z "${IN_CONTAINER}" ]; then
  VOLUMES_FROM=$(docker inspect -f '{{range .HostConfig.VolumesFrom }}--volumes-from {{ . }} {{end}}' ${IN_CONTAINER})
  VOLUMES=$(docker inspect -f '{{range .Mounts}}{{if ne "local" .Driver}}-v {{.Source}}:{{.Destination}} {{end}}{{end}}' ${IN_CONTAINER})
  VOLUMES="${VOLUMES} -v $DOCKER_HOST:$DOCKER_HOST"
  DOCKER_BINARY="$(which docker)"
  if [ ! -z "${DOCKER_BINARY}" ]; then
    VOLUMES="${VOLUMES} -v ${DOCKER_BINARY}:/bin/docker"
  fi
else
  if [ -S "$DOCKER_HOST" ]; then
      DOCKER_ADDR="-v $DOCKER_HOST:$DOCKER_HOST -e DOCKER_HOST"
  else
      DOCKER_ADDR="-e DOCKER_HOST -e DOCKER_TLS_VERIFY -e DOCKER_CERT_PATH"
  fi
  
  # Setup volume mounts for compose config and context
  if [ "$(pwd)" != '/' ]; then
      VOLUMES="-v $(pwd):$(pwd)"
  fi
  if [ -n "$COMPOSE_FILE" ]; then
      compose_dir=$(realpath $(dirname $COMPOSE_FILE))
  fi
  # TODO: also check --file argument
  if [ -n "$compose_dir" ]; then
      VOLUMES="$VOLUMES -v $compose_dir:$compose_dir"
  fi
  if [ -n "$HOME" ]; then
      VOLUMES="$VOLUMES -v $HOME:$HOME -v $HOME:/root" # mount $HOME in /root to share docker.config
  fi
fi

# Only allocate tty if we detect one
if [ -t 1 ]; then
  DOCKER_RUN_OPTIONS="-t"
fi
if [ -t 0 ]; then
  DOCKER_RUN_OPTIONS="$DOCKER_RUN_OPTIONS -i"
fi

#docker inspect $HOSTNAME
echo exec docker run --rm $DOCKER_RUN_OPTIONS $DOCKER_ADDR $COMPOSE_OPTIONS $VOLUMES_FROM $VOLUMES -w "$(pwd)" $IMAGE "$@"
exec docker run --rm $DOCKER_RUN_OPTIONS $DOCKER_ADDR $COMPOSE_OPTIONS $VOLUMES_FROM $VOLUMES -w "$(pwd)" $IMAGE "$@"
