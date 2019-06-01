FROM python:3.7-alpine

COPY basicload.py ./

RUN apk --no-cache add apache2-utils

CMD ["python", "basicload.py"] 
