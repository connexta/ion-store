FROM openjdk:11-jre-slim
LABEL maintainer=connexta
LABEL com.connexta.application.name=multi-int-store
ARG JAR_FILE
COPY ${JAR_FILE} /multi-int-store
ENTRYPOINT ["/multi-int-store"]
EXPOSE 8080