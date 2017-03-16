#!/usr/bin/env bash

: ${PRIVATE_REGISTRY:="192.168.6.17/"}
: ${GRID_VERSION:=":1.2-SNAPSHOT"}

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
docker-compose kill || true
docker-compose rm -f || true
docker-compose build || exit 1
EXIT_CODE=$?
# docker rmi -f $(docker images -f dangling=true | awk '/none/ {print $3}') 2>/dev/null || true

exit ${EXIT_CODE}
