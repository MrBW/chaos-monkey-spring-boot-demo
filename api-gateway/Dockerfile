FROM harisekhon/ubuntu-java
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
  #ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=docker"]

ENV JAVA_OPTS="-Xmx128m -Xms64m"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.profiles.active=docker"]
