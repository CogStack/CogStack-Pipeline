FROM java:8
MAINTAINER amos.folarin@kcl.ac.uk
RUN apt-get update && apt-get install -y imagemagick --fix-missing  tesseract-ocr

RUN mkdir -p /usr/src/
WORKDIR /usr/src/



COPY ./gradle/wrapper /usr/src/gradle/wrapper
COPY ./gradlew /usr/src/
RUN ./gradlew --version

COPY ./build.gradle ./settings.gradle /usr/src/


COPY . /usr/src

RUN ./gradlew build



COPY ./docker-cogstack/cogstack/ /usr/src/

ENV LOG_LEVEL info
ENV FILE_LOG_LEVEL off
ENV LOG_FILE_NAME log


CMD ./test2.sh /usr/src/build/libs/cogstack-*.jar /usr/src/docker-cogstack/cogstack/cogstack_conf
#CMD ./test.sh
