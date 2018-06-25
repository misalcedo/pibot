FROM resin/raspberrypi3-alpine-node:8-onbuild

RUN bower install

EXPOSE 80

CMD ["polymer", "serve", "-p 80", "-H 0.0.0.0"]
