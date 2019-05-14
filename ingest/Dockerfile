FROM openjdk:11-jre-slim
VOLUME /tmp
COPY /build/libs/* app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
