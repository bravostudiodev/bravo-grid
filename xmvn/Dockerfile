FROM maven:3.3.9-jdk-8-alpine
MAINTAINER IgorV

RUN apk add --no-cache docker xvfb
ENV DISPLAY=:99.0
ADD xmvn.sh /usr/bin/xmvn
ADD docker-compose.sh /usr/bin/docker-compose
RUN chmod a+xr /usr/bin/xmvn /usr/bin/docker-compose

ENTRYPOINT ["xmvn"]

#docker build -t bravo/xmvn:3.3.9 .
#docker run --rm bravo/xmvn:3.3.9 --help
