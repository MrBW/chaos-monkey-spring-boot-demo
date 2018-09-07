FROM harisekhon/ubuntu-java
VOLUME /tmp
RUN sh -c 'apt-get update'
RUN sh -c 'apt-get install iproute2 -qq'
RUN sh -c 'apt-get install net-tools -qq'
RUN sh -c 'apt-get install sudo -qq'
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]