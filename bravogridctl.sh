#!/usr/bin/env bash

: ${PRIVATE_REGISTRY:="192.168.6.17/"}
GRID_VERSION=3.1.0-astatine
RUN_PREFIX=bravo_

case "$1" in
    start|stop)
        ;;
    *)
        echo "Unknown command '$1'"
        echo "Usage bravogridctl.sh start|stop"
        exit 1
        ;;
esac

case "$OSTYPE" in
    msys*|mingw*|cygwin*)
        #echo "It's a docker toolbox, shell must be configured for docker"
        export MSYS_NO_PATHCONV=1
        docker-machine start 1> /dev/null 2> /dev/null
        eval $(docker-machine env --shell bash 2> /dev/null)
        : ${GRID_HUB_IP:=$(docker-machine url | cut -d '/' -f 3 | cut -d ':' -f 1)}
        ;;
    *)
        ;;
esac

function run_chrome_containers {
     for i in $(seq 0 $(($1-1))); do
        port=$((6000+$i))
        echo starting chrome container with port 5900 mapped to ${port}
        docker run -d -p ${port}:5900 -v /dev/shm:/dev/shm -v /dev/urandom:/dev/random --name ${RUN_PREFIX}chrome${i} -e HUB_PORT_4444_TCP_ADDR=${HUB_IP} -e HUB_PORT_4444_TCP_PORT=4444 \
         -e NODE_MAX_INSTANCES=1 -e NODE_MAX_SESSION=1 ${PRIVATE_REGISTRY}bravo/grid/chrome${SUFIX}:${GRID_VERSION} || exit 1
    done
}

function run_firefox_containers {
     for i in $(seq 0 $(($1-1))); do
        port=$((7000+$i))
        echo starting firefox container with port 5900 mapped to ${port}
        docker run -d -p ${port}:5900 -v /dev/urandom:/dev/random --name ${RUN_PREFIX}firefox${i} -e HUB_PORT_4444_TCP_ADDR=${HUB_IP} -e HUB_PORT_4444_TCP_PORT=4444 \
         -e NODE_MAX_INSTANCES=1 -e NODE_MAX_SESSION=1 ${PRIVATE_REGISTRY}bravo/grid/firefox${SUFIX}:${GRID_VERSION} || exit 1
    done
}

if [ "$1" == "stop" ]; then
    docker rm -f $(docker ps -a | grep bravo/grid | cut -d ' ' -f 1) 2> /dev/null
    docker rm -f $(docker ps -a | grep ${RUN_PREFIX}hub | cut -d ' ' -f 1) 2> /dev/null
    docker rm -f $(docker ps -a | grep ${RUN_PREFIX}firefox | cut -d ' ' -f 1) 2> /dev/null
    docker rm -f $(docker ps -a | grep ${RUN_PREFIX}chrome | cut -d ' ' -f 1) 2> /dev/null
elif [ "$1" == "start" ]; then
	if [ ! "$2" == "-no-debug" ]; then
		SUFIX="-debug"
	fi
    docker run -d -p 4444:4444 --name ${RUN_PREFIX}hub -h hub ${PRIVATE_REGISTRY}bravo/grid/hub:${GRID_VERSION} || exit 1
    HUB_IP=$(docker inspect ${RUN_PREFIX}hub | grep \"IPAddress\" | tail -1 | cut -d '"' -f 4)
    : ${GRID_HUB_IP:=${HUB_IP}} # on docker toolbox this is already set few line earlier

    : ${CHSCALE:=1}
    run_chrome_containers ${CHSCALE}

    : ${FFSCALE:=1}
    run_firefox_containers ${FFSCALE}

    echo "http://${GRID_HUB_IP}:4444/wd/hub"
fi

exit 0
