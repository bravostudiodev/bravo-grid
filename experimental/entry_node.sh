#!/usr/bin/env bash

#run with
# docker run --expose=4444 ...

source ${APP_HOME}/.noderc

if [ -z "$HUB_PORT_4444_TCP_ADDR" ]; then
  echo Not linked with a running Hub container 1>&2
  exit 1
fi

: ${NODE_MAX_INSTANCES:=1} # As integer, maps to "maxInstances"
: ${NODE_MAX_SESSION:=1} # As integer, maps to "maxSession"
: ${NODE_PORT:=5555} # As integer, maps to "port"
: ${NODE_REGISTER_CYCLE:=5000} # In milliseconds, maps to "registerCycle"
: ${NODE_POLLING:=5000} # In milliseconds, maps to "nodePolling"
: ${NODE_UNREGISTER_IF_STILL_DOWN_AFTER:=60000} # In milliseconds, maps to "unregisterIfStillDownAfter"
: ${NODE_DOWN_POLLING_LIMIT:=2} # As integer, maps to "downPollingLimit"
: ${NODE_APPLICATION_NAME:=""} # As string, maps to "applicationName"

CONF=${HOME}/selenium/config.json

cat > ${CONF} <<_EOF
{
    "capabilities": [ {
        "version": "${NODE_BROWSER_VERSION}",
        "browserName": "${NODE_BROWSER_NAME}",
        "maxInstances": ${NODE_MAX_INSTANCES},
        "seleniumProtocol": "WebDriver",
        "extension.bravoCapability": true,
        "applicationName": "${NODE_APPLICATION_NAME}"
    }],
    "proxy": "org.openqa.grid.selenium.proxy.DefaultRemoteProxy",
    "maxSession": ${NODE_MAX_SESSION},
    "port": ${NODE_PORT},
    "register": true,
    "registerCycle": ${NODE_REGISTER_CYCLE},
    "sevlets": [
        "com.bravostudiodev.grid.BravoExtensionServlet"
    ],
    "nodePolling": ${NODE_POLLING},
    "unregisterIfStillDownAfter": ${NODE_UNREGISTER_IF_STILL_DOWN_AFTER},
    "downPollingLimit": ${NODE_DOWN_POLLING_LIMIT}
}
_EOF

echo "starting node with configuration:"
cat ${CONF}
echo "SE_OPTS=${SE_OPTS}"

function shutdownhandler {
    echo "shutting down node.."
    kill -s SIGTERM ${SSHD_PID}
    killall startfluxbox
    kill -s SIGTERM ${NODE_PID}

    wait ${SSHD_PID}
    wait ${XPRA_PID}
    wait ${NODE_PID}
    echo "shutdown complete"
}

java ${JAVA_OPTS} -cp "${HOME}/selenium/*" org.openqa.grid.selenium.GridLauncherV3 \
    -role node \
    -hub http://${HUB_PORT_4444_TCP_ADDR}:${HUB_PORT_4444_TCP_PORT}/grid/register \
    -nodeConfig ${CONF} \
    ${SE_OPTS} &
NODE_PID=$!

rm -f /tmp/.X*lock

XORG_COMMAND="Xorg -dpi 96 -noreset -nolisten tcp +extension GLX +extension RANDR +extension RENDER -logfile ${HOME}/XpraXorg-10.log -config ${HOME}/xorg.conf"
XPRA_INITENV_COMMAND="xpra initenv" xpra --no-daemon --no-mdns --no-pulseaudio --xvfb="${XORG_COMMAND}" \
    start-desktop ${DISPLAY} --exit-with-child --start-child=startfluxbox&
XPRA_PID=$!

xrandr --output default --mode "${SCREEN_WIDTH}${SCREEN_HEIGHT}"

/usr/sbin/sshd -f sshd_config -D -e &
SSHD_PID=$!

trap shutdownhandler SIGTERM SIGINT
wait ${NODE_PID}

kill -s SIGTERM ${SSHD_PID}
killall startfluxbox
wait ${SSHD_PID}
wait ${XPRA_PID}
