FROM resin/raspberrypi3-python:3-onbuild

RUN apt-get update && apt-get install -y sense-hat python3-flask
RUN rm -f /usr/local/bin/python && ln -s /usr/bin/python3.4 /usr/local/bin/python

COPY ./src/main/python /usr/src/app

ENV FLASK_APP=service.py FLASK_DEBUG=1

CMD flask run --host=0.0.0.0 --port 80 --with-threads