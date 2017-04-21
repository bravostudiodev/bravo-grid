#!/usr/bin/env bash

: ${CONTAINER_PREFIX:="bravo_"}
: ${PRIVATE_REGISTRY:="192.168.6.17/"}
: ${GRID_VERSION:=":1.3-SNAPSHOT"}

case "$1" in
    start|stop|print-grid-ip)
        ;;
    *)
        echo "Unknown command '$1'"
        echo "Usage bravogridctl.sh start|stop|print-grid-ip"
        exit 1
        ;;
esac

if [ "$1" == "print-grid-ip" ]; then
	case "$OSTYPE" in
		msys*|mingw*|cygwin*)
			export MSYS_NO_PATHCONV=1
			docker-machine env | sed -n 's/^.*DOCKER_HOST=.*\/\/\(\([0-9]\|[.]\)*\).*$/\1/p'
			;;
		*)
			docker inspect -f "{{.NetworkSettings.IPAddress}}" ${CONTAINER_PREFIX}hub
			;;
	esac
	exit 0
fi

case "$OSTYPE" in
    msys*|mingw*|cygwin*)
        # echo "Running on a docker toolbox, shell must be configured for docker"
        export MSYS_NO_PATHCONV=1
        docker-machine start 1> /dev/null 2> /dev/null
        eval $(docker-machine env --shell bash 2> /dev/null)
        : ${GRID_HUB_IP:=$(docker-machine url | cut -d '/' -f 3 | cut -d ':' -f 1)}
        ;;
    *)
        # echo "With linux docker it is expected that shell is already configured for docker"
        ;;
esac

function run_chrome_containers {
     for i in $(seq 0 $(($1-1))); do
        port=$((6000+i))
        echo starting chrome container with port 5900 mapped to ${port}
        docker run -d -p ${port}:5900 -v /dev/shm:/dev/shm -v /dev/urandom:/dev/random --name ${CONTAINER_PREFIX}chrome${i} -e HUB_PORT_4444_TCP_ADDR=${HUB_IP} -e HUB_PORT_4444_TCP_PORT=4444 \
         -e NODE_MAX_INSTANCES=1 -e NODE_MAX_SESSION=1 ${PRIVATE_REGISTRY}bravo/grid/chrome${SUFIX}${GRID_VERSION} || exit 1
    done
}

function run_firefox_containers {
     for i in $(seq 0 $(($1-1))); do
        port=$((7000+i))
        echo starting firefox container with port 5900 mapped to ${port}
        docker run -d -p ${port}:5900 -v /dev/urandom:/dev/random --name ${CONTAINER_PREFIX}firefox${i} -e HUB_PORT_4444_TCP_ADDR=${HUB_IP} -e HUB_PORT_4444_TCP_PORT=4444 \
         -e NODE_MAX_INSTANCES=1 -e NODE_MAX_SESSION=1 ${PRIVATE_REGISTRY}bravo/grid/firefox${SUFIX}${GRID_VERSION} || exit 1
    done
}

if [ "$1" == "stop" ]; then
    docker rm -f $(docker ps -a | grep ${CONTAINER_PREFIX}firefox | cut -d ' ' -f 1) 2> /dev/null
    docker rm -f $(docker ps -a | grep ${CONTAINER_PREFIX}chrome | cut -d ' ' -f 1) 2> /dev/null
    docker rm -f $(docker ps -a | grep ${CONTAINER_PREFIX}hub | cut -d ' ' -f 1) 2> /dev/null
elif [ "$1" == "start" ]; then
	if [ ! "$2" == "-no-debug" ]; then
		SUFIX="-debug"
	fi
    : ${CHSCALE:=1}
    : ${FFSCALE:=1}
	GRID_MAX_SESSIONS=$((CHSCALE+FFSCALE))
    echo "CHSCALE=${CHSCALE}, FFSCALE=${FFSCALE}, GRID_MAX_SESSIONS=${GRID_MAX_SESSIONS}"
    docker run -d -p 4444:4444 -e GRID_MAX_SESSION=${GRID_MAX_SESSIONS} --name ${CONTAINER_PREFIX}hub -h hub ${PRIVATE_REGISTRY}bravo/grid/hub${GRID_VERSION} || exit 1
    HUB_IP=$(docker inspect ${CONTAINER_PREFIX}hub | grep \"IPAddress\" | tail -1 | cut -d '"' -f 4)
    : ${GRID_HUB_IP:=${HUB_IP}} # on docker toolbox this is already set few lines earlier

    run_chrome_containers ${CHSCALE}

    run_firefox_containers ${FFSCALE}

    echo "http://${GRID_HUB_IP}:4444/wd/hub"
fi

exit 0
