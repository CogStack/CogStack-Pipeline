FROM java:8
MAINTAINER richgjackson@gmail.com
WORKDIR /usr/src/
#uncomment approrpiately if you dont want to DL the tar each time
ADD 'http://bio-lark.org/hpo/hpo_cr_web.tar.gz' /usr/src
#COPY 'hpo_cr_web.tar.gz' /usr/src/
RUN ["tar", "-xvf" ,"/usr/src/hpo_cr_web.tar.gz"]
RUN ["rm","/usr/src/hpo_cr_web.tar.gz"]
WORKDIR /usr/src/hpo_cr_web
COPY ./application-prod.yml /usr/src/hpo_cr_web/application-prod.yml
COPY ./start.sh /usr/src/hpo_cr_web/start.sh
CMD ["sh","/usr/src/hpo_cr_web/start.sh"]
