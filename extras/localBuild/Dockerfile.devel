ARG VERSION

FROM cogstacksystems/cogstack-java-devel:${VERSION}

# setup the build environment
#
RUN mkdir -p /devel
WORKDIR /devel

COPY ./gradle/wrapper /devel/gradle/wrapper
COPY ./gradlew /devel/

RUN ./gradlew --version

COPY ./build.gradle ./settings.gradle /devel/
COPY . /devel/


# build cogstack
RUN ./gradlew bootJar --no-daemon

#CMD ./gradlew bootRepackage
