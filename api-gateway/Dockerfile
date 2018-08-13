FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENV JAVA_OPTS="-Xmx128m -Xms128m"
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=docker"]