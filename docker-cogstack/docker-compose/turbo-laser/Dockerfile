FROM java:8
RUN apt-get update && apt-get install -y imagemagick --fix-missing  tesseract-ocr

COPY ./turbolaser/ /usr/src/turbolaser
RUN mkdir -p /usr/src/turbolaser_conf
ENV LOG_LEVEL info
ENV FILE_LOG_LEVEL off
ENV LOG_FILE_NAME log
WORKDIR /usr/src/turbolaser

CMD ./wait-for-it.sh postgres:5432 --timeout=30 --strict -- java -DLOG_FILE_NAME=$LOG_FILE_NAME -DLOG_LEVEL=$LOG_LEVEL -DFILE_LOG_LEVEL=$FILE_LOG_LEVEL -jar /usr/src/turbolaser/turbo-laser-0.3.0.jar /usr/src/turbolaser_conf
