FROM registry.nexus.sprouts.kr/openjdk:11-jre-slim

LABEL authors="jwseo@live.co.kr"

ARG PROJECT_NAME
ARG VERSION

VOLUME /tmp

ADD ./target/$PROJECT_NAME-$VERSION.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]

EXPOSE 8080
