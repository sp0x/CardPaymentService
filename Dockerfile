FROM maven:3.5-jdk-8-alpine AS build

MAINTAINER Vasil Vasilev <vasil.vasilev@smartmodule.ch>

RUN mkdir -p /usr/src
WORKDIR /usr/src/recurring
COPY /pom.xml /usr/src/recurring
COPY src /usr/src/recurring/src
COPY libs /usr/src/recurring/libs
RUN mvn package -DskipTests=true -Dmaven.test.skip=true


FROM openjdk:8-jre-alpine
COPY --from=build /usr/src/recurring/target/recurring.jar app.jar

CMD ["-jar", "/app.jar"]
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom"]