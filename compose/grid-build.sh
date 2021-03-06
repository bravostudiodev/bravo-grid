#!/usr/bin/env bash

: ${PRIVATE_REGISTRY:="192.168.6.17/"}
: ${GRID_VERSION:=":1.4-SNAPSHOT"}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
#SCRIPT_NAME="$(basename "$(test -L "$0" && readlink "$0" || echo "$0")")"

# ensure docker machine is running and configure current shell for docker
case "$OSTYPE" in
    msys*|mingw*|cygwin*)
		#It's a docker toolbox, shell must be configured for docker
        export MSYS_NO_PATHCONV=1
        docker-machine start 1> /dev/null 2> /dev/null
        eval $(docker-machine env --shell bash 2> /dev/null)
        ;;
    *)
        ;;
esac

if [ "$1" == "run" ]; then
    : ${CHSCALE:="1"}
    : ${FFSCALE:="1"}
    echo "SUM_OF_CHSCALE_AND_FFSCALE=$((CHSCALE+FFSCALE))" >> ".env"
fi

echo Building compose file: "${SCRIPT_DIR}/docker-compose.yml"
echo "PREFIX=${PRIVATE_REGISTRY}" > "${SCRIPT_DIR}/.env"
echo "GRID_VERSION=${GRID_VERSION}" >> "${SCRIPT_DIR}/.env"
unset MSYS_NO_PATHCONV
pushd "${SCRIPT_DIR}"

ENTRYPOINT="$(docker inspect -f '{{ range $val := .ContainerConfig.Entrypoint }}{{ $val }} {{ end }}' docker/compose:1.14.0 | tr -d '\n')"
echo "ENTRYPOINT=${ENTRYPOINT}"

DOCKER_COMPOSE="docker run --rm -i -v /var/run/docker.sock:/var/run/docker.sock -v /usr/bin/docker:/usr/bin/docker \
 -v $PWD:/apprun -w /apprun --entrypoint /bin/sh docker/compose:1.14.0 -c"

#PYTHONUSERBASE=${SCRIPT_DIR}
#pip install --user docker-compose

RESULT=""

if [ "$1" != "run" ]; then
    if [ "$1" == "updres" ]; then
     mvn "-Dbravogrid.version=${GRID_VERSION:1}" -f deps-hub.pom generate-resources || exit 1
     mvn "-Dbravogrid.version=${GRID_VERSION:1}" -f deps-node.pom generate-resources || exit 1
    fi
    echo "Starting docker build ..."
    RESULT=$(eval "${DOCKER_COMPOSE} \"${ENTRYPOINT} build && echo DOCKERPASSED || echo DOCKERFAILED\"")
    # EXIT_CODE=$?
    echo "... finished docker build. ${RESULT}"
else
    #${DOCKER_COMPOSE} up -d --scale chrome=${CHSCALE} --scale firefox=${FFSCALE}
    RESULT=$(eval "${DOCKER_COMPOSE} \"${ENTRYPOINT} up -d --scale chrome=${CHSCALE} --scale firefox=${FFSCALE} && echo DOCKERPASSED || echo DOCKERFAILED\"")
fi

echo "${RESULT}"
(echo ${RESULT} | grep "DOCKERPASSED") && EXIT_CODE=0 || EXIT_CODE=1
exit ${EXIT_CODE}

# export CHSCALE=2 FFSCALE=4
# echo "PREFIX=192.168.6.17/" > ".env"
# echo "SUM_OF_CHSCALE_AND_FFSCALE=$((CHSCALE+FFSCALE))" >> ".env"
# echo "GRID_VERSION=${GRID_VERSION}" >> ".env"
# $DOCKER_COMPOSE  up -d --scale firefox=${FFSCALE} --scale chrome=${CHSCALE}
