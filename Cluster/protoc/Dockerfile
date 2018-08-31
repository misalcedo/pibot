FROM ubuntu

RUN apt-get update && apt install -y protobuf-compiler

COPY src /src

WORKDIR /generated-src

RUN mkdir -p python

RUN protoc -I=/src/ --python_out=python/ /src/*.proto
