FROM java:8
MAINTAINER richgjackson@gmail.com
RUN apt-get update && apt-get install -y imagemagick --fix-missing  tesseract-ocr

RUN mkdir -p /usr/src/
WORKDIR /usr/src/

COPY . /usr/src/

ENV LOG_LEVEL info
ENV FILE_LOG_LEVEL off
ENV LOG_FILE_NAME log


CMD ./test.sh
