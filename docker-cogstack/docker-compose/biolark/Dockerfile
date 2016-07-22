FROM java:8
MAINTAINER richgjackson@gmail.com
WORKDIR /usr/src/hpo_cr_web
ADD 'http://bio-lark.org/hpo/hpo_cr_web.tar.gz' /usr/src
COPY ./application-prod.yml /usr/src/hpo_cr_web/application-prod.yml
COPY ./start.sh /usr/src/hpo_cr_web/start.sh
CMD ["sh","/usr/src/hpo_cr_web/start.sh"]
