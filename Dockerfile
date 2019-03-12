################################
#
# Java builder -- JDK
#
FROM openjdk:11-jdk-slim AS java-builder

# tesseract-ocr < 4.0 is only available from the previous Debian Stretch distribution
# for installing it plese uncomment the following lines with '###''
### RUN echo "deb http://ftp.de.debian.org/debian stretch main" >> /etc/apt/sources.list

RUN apt-get update && \
#	apt-get dist-upgrade -y && \
#	apt-get install -y tesseract-ocr && \
	apt-get install -y tesseract-ocr=4.0.0-2 tesseract-ocr-eng=1:4.00~git30-7274cfa-1 tesseract-ocr-osd=1:4.00~git30-7274cfa-1 && \
###	apt-get install -y tesseract-ocr-osd=3.04.00-1 tesseract-ocr-eng=3.04.00-1 tesseract-ocr=3.04.01-5 && \
	apt-get install -y imagemagick=8:6.9.10.14+dfsg-7 --fix-missing && \
	apt-get clean autoclean && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*


################################
#
# Java runner -- JRE
#
FROM openjdk:11-jre-slim AS java-runner

# tesseract-ocr < 4.0 is only available from the previous Debian Stretch distribution
# for installing it plese uncomment the following lines with '###''
### RUN echo "deb http://ftp.de.debian.org/debian stretch main" >> /etc/apt/sources.list

RUN apt-get update && \
#	apt-get dist-upgrade -y && \
#	apt-get install -y tesseract-ocr && \
	apt-get install -y tesseract-ocr=4.0.0-1+b1 tesseract-ocr-eng=1:4.00~git30-7274cfa-1 tesseract-ocr-osd=1:4.00~git30-7274cfa-1 && \
###	apt-get install -y tesseract-ocr-osd=3.04.00-1 tesseract-ocr-eng=3.04.00-1 tesseract-ocr=3.04.01-5 && \
	apt-get install -y imagemagick=8:6.9.10.14+dfsg-7 --fix-missing && \
	apt-get clean autoclean && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*



################################
#
# CogStack builder
#
FROM java-builder AS cogstack-builder

# setup the build environment
RUN mkdir -p /devel
WORKDIR /devel

COPY ./gradle/wrapper /devel/gradle/wrapper
COPY ./gradlew /devel/

RUN ./gradlew --version

COPY ./build.gradle ./settings.gradle /devel/
COPY . /devel/


# build cogstack
RUN ./gradlew bootJar --no-daemon



################################
#
# CogStack runner
#
FROM java-runner

# setup env
#RUN apt-get update && apt-get install -y procps
RUN mkdir -p /cogstack
WORKDIR /cogstack


# copy artifacts
COPY --from=cogstack-builder /devel/build/libs/cogstack-*.jar ./
COPY --from=cogstack-builder /devel/scripts/*.sh ./


# entry point
CMD /bin/bash
