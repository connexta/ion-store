FROM openjdk:11-jre-slim
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jarcd
ENTRYPOINT ["java",\
    "-Dserver.port=9040",\
    "-Djava.security.egd=file:/dev/./urandom",\
    "-Djava.rmi.server.hostname=cdr-ingest",\
    "-jar",\
    "/app.jar"]
EXPOSE 9040