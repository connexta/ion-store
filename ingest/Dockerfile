FROM openjdk:11-jre-slim
LABEL maintainer=connexta
LABEL com.connexta.application.name=cdr-ingest
ARG JAR_FILE
COPY ${JAR_FILE} /cdr-ingest
ENTRYPOINT ["/cdr-ingest"]
EXPOSE 8080 10040 10050
# Enable JMX so the can JVM be monitored
# NOTE: The exposed JMX port number must be the same as the port number published in the docker compose or stack file.
ENV JAVA_TOOL_OPTIONS "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:10050 \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=10040 \
-Dcom.sun.management.jmxremote.rmi.port=10040 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Djava.rmi.server.hostname=0.0.0.0 \
-Dcom.sun.management.jmxremote.local.only=false"