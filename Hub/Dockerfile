FROM resin/raspberrypi3-alpine-openjdk:8-jdk
COPY . /staging
WORKDIR /staging
RUN sh gradlew build

FROM resin/raspberrypi3-alpine-openjdk:8-jre
WORKDIR /usr/src/app
COPY --from=0 /staging/build/distributions/Hub-*.tar .
RUN tar -xf /usr/src/app/Hub-*.tar --strip-components 1 && rm /usr/src/app/Hub-*.tar
EXPOSE 80
CMD ["bin/Hub"]