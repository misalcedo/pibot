FROM resin/raspberrypi3-alpine-node:8-onbuild

RUN npm install -g polymer-cli

EXPOSE 80

CMD ["polymer", "serve", "-p 80", "-H 0.0.0.0"]