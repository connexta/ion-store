FROM openjdk:11
LABEL maintainer=connexta
LABEL com.connexta.application.name=store
ARG JAR_FILE
COPY ${JAR_FILE} /store
ENTRYPOINT ["/store"]
EXPOSE 8080 10042 10052
# Enable JMX so the can JVM be monitored
# NOTE: The exposed JMX port number must be the same as the port number published in the docker compose or stack file.
ENV JAVA_TOOL_OPTIONS "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:10052 \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=10042 \
-Dcom.sun.management.jmxremote.rmi.port=10042 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Djava.rmi.server.hostname=0.0.0.0 \
-Dcom.sun.management.jmxremote.local.only=false"