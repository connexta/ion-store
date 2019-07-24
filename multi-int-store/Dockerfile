FROM openjdk:11
LABEL maintainer=connexta
LABEL com.connexta.application.name=multi-int-store
ARG JAR_FILE
COPY ${JAR_FILE} /multi-int-store
ENTRYPOINT ["/multi-int-store"]
EXPOSE 8080 10041
# Enable JMX so the can JVM be monitored
# NOTE: The exposed JMX port number must be the same as the port number published in the docker compose or stack file.
ENV JAVA_OPTS "-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=10041 -Dcom.sun.management.jmxremote.rmi.port=10041 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=0.0.0.0 -Dcom.sun.management.jmxremote.local.only=false"