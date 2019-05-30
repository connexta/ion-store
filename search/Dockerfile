FROM openjdk:11-jre-slim
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java",\
    "-Dserver.port=9039",\
    "-Djava.security.egd=file:/dev/./urandom",\
    "-Djava.rmi.server.hostname=cdr-search",\
    "-jar",\
    "/app.jar"]
EXPOSE 9039