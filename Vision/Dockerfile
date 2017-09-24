FROM resin/raspberrypi-python:3-onbuild

COPY ./src/main/python /usr/src/app

ENV FLASK_APP service.py
ENV FLASK_DEBUG 1

ENTRYPOINT flask run --host=0.0.0.0 --port 80 --no-reload