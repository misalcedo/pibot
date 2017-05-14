FROM java:8
ENV DISTRIBUTION_NAME=Hub-1.0-SNAPSHOT
ADD ./build/distributions/$DISTRIBUTION_NAME.tar /tmp
RUN mv /tmp/$DISTRIBUTION_NAME /usr/src/app
WORKDIR /usr/src/app
ENTRYPOINT bin/Hub