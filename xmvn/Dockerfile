FROM maven:3.3.9-jdk-8-alpine
MAINTAINER IgorV

ADD xmvn.sh /usr/bin/xmvn
ADD docker-compose.sh /usr/bin/docker-compose
RUN apk add --no-cache docker xvfb \
    && chmod a+xr /usr/bin/xmvn /usr/bin/docker-compose
    && mv $(which mvn) /usr/bin/mvn.real
    && mv /usr/bin/xmvn /usr/bin/mvn
ENV DISPLAY=:99.0
RUN

ENTRYPOINT ["mvn"]

#docker build -t bravo/xmvn:3.3.9 .
#docker run --rm bravo/xmvn:3.3.9 --help
