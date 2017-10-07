FROM resin/raspberrypi3-python:3-onbuild

COPY ./src/main/python /usr/src/app

ENV FLASK_APP service.py

ENTRYPOINT flask run --host=0.0.0.0 --port 80 --with-threads