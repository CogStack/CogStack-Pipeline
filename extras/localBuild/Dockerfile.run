ARG VERSION

FROM cogstacksystems/cogstack-java-run:${VERSION}


# setup env
#
# DEBUG:
#RUN apt-get update && apt-get install -y procps
#

WORKDIR /cogstack

# copy artifacts
#
COPY --from=cogstacksystems/cogstack-java-devel:local /devel/build/libs/cogstack-*.jar ./
COPY --from=cogstacksystems/cogstack-java-devel:local /devel/scripts/*.sh ./

# entry point
#
#CMD ./test2.sh /usr/src/build/libs/cogstack-*.jar /usr/src/docker-cogstack/cogstack/cogstack_conf
CMD /bin/bash
