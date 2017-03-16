#!/usr/bin/env bash

#set -x
umask 002
/usr/bin/Xvfb ${DISPLAY} -ac -screen 0 1024x768x16 +extension RANDR 2> /dev/null &
XVFB_PID=$!
sleep 1
/usr/bin/mvn.real $@
exit_value=$?
kill -9 ${XVFB_PID} 2>&1 1>/dev/null
#echo DISPLAY=$DISPLAY
exit ${exit_value}
