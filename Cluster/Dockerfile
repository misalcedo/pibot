FROM resin/raspberrypi-alpine-python:3-onbuild

ENV FLASK_APP=src/service.py FLASK_DEBUG=1

EXPOSE 80

CMD flask run --host=0.0.0.0 --port 80