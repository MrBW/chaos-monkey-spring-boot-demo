FROM harisekhon/ubuntu-java:jdk8
COPY instana.key /
COPY enable_instana.sh /usr/local/bin
#ENV INSTANA_AGENT_TAGS=spring-sleuth-opentracing
#ENV INSTANA_AGENT_ZONE=SpringOpenTracing
RUN chmod a+x /usr/local/bin/enable_instana.sh && apt-get install apt-utils -y && apt-get install curl -y && apt-get install apt-transport-https -y && apt-get install wget -y
RUN curl -o setup_agent.sh https://setup.instana.io/agent && chmod 700 ./setup_agent.sh && ./setup_agent.sh -a $(cat /instana.key) -t dynamic -l eu -s -y
RUN rm /instana.key
COPY configuration.yaml /opt/instana/agent/etc/instana