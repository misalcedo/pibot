FROM openjdk:jdk-alpine as build-stage
COPY . /staging
WORKDIR /staging
RUN sh gradlew build

FROM openjdk:jre-alpine
COPY --from=build-stage /staging/build/distributions/Hub-*.tar /usr/src/app.tar
RUN tar -xf /usr/src/app.tar && rm /usr/src/app.tar
ENTRYPOINT /usr/src/app/bin/Hub