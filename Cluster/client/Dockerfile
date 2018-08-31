FROM misalcedo/rapbot:protoc-1.0

FROM python:3.7

WORKDIR /usr/src/app

COPY --from=0 /generated-src/python/* .

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY src .

CMD [ "python", "./client.py" ]
