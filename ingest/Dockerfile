FROM openjdk:11-jre-slim
LABEL maintainer=connexta
LABEL com.connexta.application.name=cdr-ingest
ARG JAR_FILE
COPY ${JAR_FILE} /cdr-ingest
ENTRYPOINT ["/cdr-ingest"]
EXPOSE 8080
