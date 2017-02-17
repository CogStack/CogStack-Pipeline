FROM java:8
MAINTAINER richgjackson@gmail.com
RUN mkdir -p /usr/src
WORKDIR /usr/src/
#NOTE - containter currently out of action. New Link needed from UOS
#uncomment appropriately if you dont want to DL the tar each time
#ADD 'http://www.dcs.shef.ac.uk/~genevieve/D4.5/bio-yodie-D4.5.zip' /usr/src
#use local zip if available
COPY 'bio-yodie-D1.2.1.zip /usr/src/
RUN unzip /usr/src/bio-yodie-D1.2.1.zip 
RUN rm /usr/src/bio-yodie-D1.2.1.zip
COPY gatewebservices-0.1.0.jar /usr/src/
COPY ./start.sh /usr/src/start.sh
CMD ["sh","/usr/src/start.sh"]
