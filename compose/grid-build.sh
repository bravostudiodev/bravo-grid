#!/usr/bin/env bash

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
unset MSYS_NO_PATHCONV
docker-compose -f "${SCRIPT_DIR}/docker-compose.yml" kill || true
docker-compose -f "${SCRIPT_DIR}/docker-compose.yml" rm -f || true
docker-compose -f "${SCRIPT_DIR}/docker-compose.yml" build || exit 1
EXIT_CODE=$?
# docker rmi -f $(docker images -f dangling=true | awk '/none/ {print $3}') 2>/dev/null || true

docker tag bravo/grid/hub           bravo/grid/hub:3.0.1-barium
docker tag bravo/grid/chrome        bravo/grid/chrome:3.0.1-barium
docker tag bravo/grid/chrome-debug  bravo/grid/chrome-debug:3.0.1-barium
docker tag bravo/grid/firefox       bravo/grid/firefox:3.0.1-barium
docker tag bravo/grid/firefox-debug bravo/grid/firefox-debug:3.0.1-barium

docker tag bravo/grid/hub           bravo/grid/hub:latest
docker tag bravo/grid/chrome        bravo/grid/chrome:latest
docker tag bravo/grid/chrome-debug  bravo/grid/chrome-debug:latest
docker tag bravo/grid/firefox       bravo/grid/firefox:latest
docker tag bravo/grid/firefox-debug bravo/grid/firefox-debug:latest

exit ${EXIT_CODE}
