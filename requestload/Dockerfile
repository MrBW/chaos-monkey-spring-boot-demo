FROM python:3.7-alpine

ADD ./basicload.py ./

RUN apk --no-cache add apache2-utils

ENTRYPOINT ["python", "basicload.py"] 
