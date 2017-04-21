#!/usr/bin/env bash

if [ -z "$1" ]; then
	echo "Version of selenium-server dependency not set (1st argument)"
	exit 1
fi

if [ -z "$2" ]; then
	echo "Version of original selenium docker images not set (2nd argument) - IT SHOULD MATCH WITH selenium-server dependency version (1st argument)"
	exit 1
fi

if [ -z "$3" ]; then
	echo "Version for output selenium docker images with integrated bravo extensions not set (3rd argument)"
	exit 1
fi

mvn --version 1>/dev/null 2>&1 || { echo "mvn executable not found in path"; exit 1; }

mvn versions:update-property "-Dproperty=seleniumsrv.version" "-DnewVersion=[$1]"

SCRIPT_DIR="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"
sed -i "s/\(FROM.*:\).*/\1$2/" ${SCRIPT_DIR}/compose/Dockerfile.*

mvn versions:set -DnewVersion=$3
sed -i "s/GRID_VERSION:=\":.*\"/GRID_VERSION:=\":$3\"/" "${SCRIPT_DIR}/compose/grid-build.sh" "${SCRIPT_DIR}/bravogridctl.sh"
