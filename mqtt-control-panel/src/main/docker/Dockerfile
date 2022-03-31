FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD mqtt-control-panel-2.1.6.RELEASE.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]