FROM java:8
MAINTAINER richgjackson@gmail.com
RUN apt-get update && apt-get install -y imagemagick --fix-missing  tesseract-ocr

COPY ./turbolaser/ /usr/src/turbolaser
RUN mkdir -p /usr/src/turbolaser_conf
ENV LOG_LEVEL info
ENV FILE_LOG_LEVEL off
ENV LOG_FILE_NAME log
WORKDIR /usr/src/turbolaser

CMD ./test.sh
