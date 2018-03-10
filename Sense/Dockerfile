FROM resin/raspberrypi3-python:3-onbuild

RUN apt-get update && apt-get install -y sense-hat python3-dev python3-setuptools
RUN rm -f /usr/local/bin/python && ln -s /usr/bin/python3.4 /usr/local/bin/python

RUN mkdir -p /staging/Flask && cd /staging/Flask \
    && git clone http://github.com/pallets/flask.git && cd flask \
    && python setup.py build && python setup.py install \
    && cd /usr/src/app && rm -fr /staging/Flask

COPY ./src/main/python /usr/src/app
COPY ./RTIMULib.ini /etc/RTIMULib.ini

ENV FLASK_APP=service.py FLASK_DEBUG=1

EXPOSE 80

CMD flask run --host=0.0.0.0 --port 80 --with-threads