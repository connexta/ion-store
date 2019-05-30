FROM openjdk:11-jre-slim
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java",\
    "-Dserver.port=9041",\
    "-Djava.security.egd=file:/dev/./urandom",\
    "-Djava.rmi.server.hostname=cdr-multi-int-store",\
    "-jar",\
    "/app.jar"]
EXPOSE 9041