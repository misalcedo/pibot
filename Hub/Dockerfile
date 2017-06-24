FROM openjdk:jdk-alpine
#FROM resin/raspberry-pi-openjdk:8-jdk

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN sh gradlew build

ENTRYPOINT sh gradlew run