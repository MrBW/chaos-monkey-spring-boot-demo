FROM docker-base-image
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS="-Xmx128m -Xms64m"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.profiles.active=docker"]
