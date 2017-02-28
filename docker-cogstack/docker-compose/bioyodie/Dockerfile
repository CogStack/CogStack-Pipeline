FROM java:8
MAINTAINER richgjackson@gmail.com
RUN mkdir -p /usr/src

WORKDIR /usr/src/

#uncomment appropriately if you dont want to DL the tar each time
ADD 'http://www.dcs.shef.ac.uk/~genevieve/D4.5/bio-yodie-D4.5.zip' /usr/src
#use local zip if available
#COPY bio-yodie-D4.5.zip /usr/src/
RUN unzip /usr/src/bio-yodie-D4.5.zip && rm /usr/src/bio-yodie-D4.5.zip

ADD https://downloads.sourceforge.net/project/gate/gate/8.3/gate-8.3-build5704-BIN.zip /usr/src/
#use local zip if available
#COPY gate-8.3-build5704-BIN.zip /usr/src/
RUN unzip gate-8.3-build5704-BIN.zip && rm gate-8.3-build5704-BIN.zip

ADD https://github.com/RichJackson/gatewebservices/releases/download/0.1.1/gatewebservices-0.1.1.jar /usr/src/
#use local jar if available
#COPY gatewebservices-0.1.1.jar /usr/src/


COPY ./start.sh /usr/src/start.sh
CMD ["sh","/usr/src/start.sh"]
