FROM openjdk:11-jre-slim
LABEL maintainer=connexta
LABEL com.connexta.application.name=cdr-search
ARG JAR_FILE
COPY ${JAR_FILE} /cdr-search
ENTRYPOINT ["/cdr-search"]
EXPOSE 8080