FROM docker-base-image
ARG SERVICE_NAME

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENV SERVICE_NAME="${SERVICE_NAME}"
LABEL APP=${SERVICE_NAME}
LABEL DOMAIN="shopping-demo"

ENV JAVA_OPTS="-Xmx64m -Xms32m"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.profiles.active=docker"]
