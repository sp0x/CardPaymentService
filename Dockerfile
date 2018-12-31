FROM maven:3.5-jdk-8-alpine AS build

MAINTAINER Vasil Vasilev <vasil.vasilev@smartmodule.ch>

RUN mkdir -p /usr/src
RUN mkdir -p /usr/share/maven/ref
WORKDIR /usr/src/recurring
COPY /pom.xml /usr/src/recurring
COPY libs libs
COPY repo repo

# Download the package and make it cached in docker image
RUN mvn -B -f ./pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve

COPY src src
COPY libs libs
RUN mvn package -DskipTests=true -Dmaven.test.skip=true


FROM openjdk:8-jre-alpine
COPY --from=build /usr/src/recurring/target/recurring-jar-with-dependencies.jar app.jar
COPY --from=build /usr/src/recurring/libs/ecomm_merchant.jar /lib/ecomm_merchant.jar

CMD ["-jar", "/app.jar"]
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dfile.encoding=UTF-8"]