FROM openjdk:8-jdk-alpine
MAINTAINER IgorV <gorvin@users.noreply.github.com>

ARG APP_USER=app
ENV APP_USER=${APP_USER}

RUN apk add --no-cache openssh \
	&& mkdir /var/run/sshd \
	&& ssh-keygen -f /etc/ssh/ssh_host_rsa_key -N '' -t rsa \
    && ssh-keygen -f /etc/ssh/ssh_host_dsa_key -N '' -t dsa \
	&& ssh-keygen -f /etc/ssh/ssh_host_ecdsa_key -N '' -t ecdsa \
	&& ssh-keygen -f /etc/ssh/ssh_host_ed25519_key -N '' -t ed25519 \
	&& chmod 700 -R /etc/ssh/

RUN set -x \
	&& addgroup -g 899 -S ${APP_USER} \
	&& adduser -u 899 -D -S -s /bin/ash -G ${APP_USER} ${APP_USER} \
	&& echo ${APP_USER}:secret | chpasswd \
	&& mkdir -p /home/${APP_USER}/etc/ssh \
	&& cp /etc/ssh/sshd_config /home/${APP_USER}/etc/ssh/ \
	&& sed -e 's/.*UsePrivilegeSeparation.*/UsePrivilegeSeparation no/' \
        -e 's/.*LogLevel.*/LogLevel DEBUG/' \
        -e 's/.*StrictModes.*/StrictModes no/' \
        -e 's/.*Port.*/Port 2200/' \
        -e "s@.*PidFile.*@PidFile /home/${APP_USER}/var/run/sshd.pid@" \
        -i /home/${APP_USER}/etc/ssh/sshd_config \
    && chown -R ${APP_USER}:${APP_USER} /home/${APP_USER}/etc/ssh/

EXPOSE 2200

USER ${APP_USER}
RUN ssh-keygen -f /home/${APP_USER}/etc/ssh/ssh_host_rsa_key -N '' -t rsa \
    && ssh-keygen -f /home/${APP_USER}/etc/ssh/ssh_host_dsa_key -N '' -t dsa \
	&& ssh-keygen -f /home/${APP_USER}/etc/ssh/ssh_host_ecdsa_key -N '' -t ecdsa \
	&& ssh-keygen -f /home/${APP_USER}/etc/ssh/ssh_host_ed25519_key -N '' -t ed25519 \
	&& chmod 700 -R /home/${APP_USER}/etc/ssh/

WORKDIR /home/${APP_USER}/
ENTRYPOINT ["/usr/sbin/sshd", "-f", "etc/ssh/sshd_config", "-D", "-e"]
# hhttps://unix.stackexchange.com/questions/256625/need-the-sshd-server-be-run-in-root-privilege-for-a-password-less-login
#     -D	     When this option is specified, sshd will not detach and does not
#	     become a daemon.  This allows easy	monitoring of sshd.
#
#     -d	     Debug mode.  The server sends verbose debug output	to standard
#	     error, and	does not put itself in the background.	The server
#	     also will not fork	and will only process one connection.  This
#	     option is only intended for debugging for the server.  Multiple
#	     -d	options	increase the debugging level.  Maximum is 3.
#
#     -E	log_file
#	     Append debug logs to log_file instead of the system log.
#
#     -e	     Write debug logs to standard error	instead	of the system log.