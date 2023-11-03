FROM registry.nexus.devops.local/eclipse-temurin:17-jdk-alpine

LABEL authors="jwseo@live.co.kr"

ARG PROJECT_NAME
ARG VERSION

VOLUME /tmp

ADD ./target/$PROJECT_NAME-$VERSION.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]

EXPOSE 8080
