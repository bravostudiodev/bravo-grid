#!/usr/bin/env bash

SCRIPT_DIR="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"
SCRIPT_NAME="$(basename "$(test -L "$0" && readlink "$0" || echo "$0")")"
TEMPVOLUME="$(mktemp -d)"
case "$OSTYPE" in
    msys*|mingw*|cygwin*)
		#It's a docker toolbox, shell must be configured for docker
        export MSYS_NO_PATHCONV=1
        docker-machine start 1> /dev/null 2> /dev/null
        eval $(docker-machine env --shell bash 2> /dev/null)
		TEMPVOLUME="$(mount | grep $TEMP | sed -e 's/\([A-Z]\):\(.*\) on .*/\/\l\1\2/')/$(basename ${TEMPVOLUME})"
        ;;
    *)
        ;;
esac

docker build -t bravo/xmvn:3.3.9 ./xmvn

cp -a ./bravo-*/ ./deps-*/ ./compose/ ./pom.xml ${TEMPVOLUME}/
M2VOLUME="$(echo ~)/.m2"
VOLUMES="-v ${TEMPVOLUME}:/bravo-grid-workspace -v ${M2VOLUME}:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock"

if [ -z "$@" ]; then
	MVN_ARGS="install"
else
	MVN_ARGS="$@"
fi

#docker run --rm ${VOLUMES} -w /workspace bravo/xmvn:3.3.9 -B versions:set -DnewVersion=1.0
echo "docker run --rm ${VOLUMES} -w /workspace bravo/xmvn:3.3.9 ${MVN_ARGS}"
docker run --rm ${VOLUMES} -w /bravo-grid-workspace bravo/xmvn:3.3.9 ${MVN_ARGS}

if [[ ${MVN_ARGS} == *"versions:set"* ]]; then
	cp -af ${TEMPVOLUME}/* ./
fi

rm -rf ${TEMPVOLUME}
