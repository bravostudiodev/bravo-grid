#!/usr/bin/env bash

#run with
# docker run --expose=4444 ...

: ${GRID_MAX_SESSION:=5} # As integer, maps to "maxSession"
: ${GRID_NEW_SESSION_WAIT_TIMEOUT:=-1} # In milliseconds, maps to "newSessionWaitTimeout"
: ${GRID_THROW_ON_CAPABILITY_NOT_PRESENT:=true} # As a boolean, maps to "throwOnCapabilityNotPresent"
: ${GRID_JETTY_MAX_THREADS:=-1} # As an integer
: ${GRID_CLEAN_UP_CYCLE:=5000} # In milliseconds, maps to "cleanUpCycle"
: ${GRID_BROWSER_TIMEOUT:=0} # In seconds, maps to "browserTimeout"
: ${GRID_TIMEOUT:=30} # In seconds, maps to "timeout"
: ${GRID_DEBUG:=false} # Debug

CONF=${HOME}/selenium/config.json
cat >${CONF} <<_EOF
{
    "host": null,
    "port": 4444,
    "role": "hub",
    "maxSession": ${GRID_MAX_SESSION},
    "newSessionWaitTimeout": ${GRID_NEW_SESSION_WAIT_TIMEOUT},
    "capabilityMatcher": "com.bravostudiodev.grid.CustomCapabilityMatcher",
    "sevlets": [
        "com.bravostudiodev.grid.HubRequestsProxyingServlet"
    ],
    "throwOnCapabilityNotPresent": ${GRID_THROW_ON_CAPABILITY_NOT_PRESENT},
    "jettyMaxThreads": ${GRID_JETTY_MAX_THREADS},
    "cleanUpCycle": ${GRID_CLEAN_UP_CYCLE},
    "browserTimeout": ${GRID_BROWSER_TIMEOUT},
    "timeout": ${GRID_TIMEOUT},
    "debug": ${GRID_DEBUG}
}
_EOF

echo "starting selenium hub with configuration:"
cat $CONF
echo "SE_OPTS=${SE_OPTS}"

function shutdownhandler {
    echo "shutting down hub.."
    kill -s SIGTERM ${NODE_PID}
    wait ${NODE_PID}
    echo "shutdown complete"
}

java ${JAVA_OPTS} -cp "${HOME}/selenium/*" org.openqa.grid.selenium.GridLauncherV3 \
  -role hub \
  -hubConfig ${CONF} \
  ${SE_OPTS} &
NODE_PID=$!

trap shutdownhandler SIGTERM SIGINT
wait ${NODE_PID}
