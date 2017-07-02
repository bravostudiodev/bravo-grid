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

echo Building compose file: "${SCRIPT_DIR}/docker-compose.yml"
echo "PRIVATE_REGISTRY=${PRIVATE_REGISTRY}" > "${SCRIPT_DIR}/.env"
echo "GRID_VERSION=${GRID_VERSION}" >> "${SCRIPT_DIR}/.env"
unset MSYS_NO_PATHCONV
pushd "${SCRIPT_DIR}"
DOCKE_COMPOSE="docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v /usr/bin/docker:/usr/bin/docker \
 -v $PWD:/apprun -w /apprun docker/compose:1.14.0"
${DOCKE_COMPOSE} -f docker-compose-japp.yml build || exit 1
if [ "$1" == "updres" ]; then
 mvn -f deps-hub.pom generate-resources || exit 1
 mvn -f deps-node.pom generate-resources || exit 1
fi
${DOCKE_COMPOSE} build || exit 1
EXIT_CODE=$?
# docker rmi -f $(docker images -f dangling=true | awk '/none/ {print $3}') 2>/dev/null || true

exit ${EXIT_CODE}

# export CHSCALE=2 FFSCALE=4
# echo "PRIVATE_REGISTRY=192.168.6.17" > ".env"
# echo "SUM_OF_CHSCALE_AND_FFSCALE=$((CHSCALE+FFSCALE))" >> ".env"
# echo "GRID_VERSION=${GRID_VERSION}" >> ".env"
# docker run --rm -it docker/compose:1.13.0 up -d --scale firefox=${FFSCALE} --scale chrome=${CHSCALE}
