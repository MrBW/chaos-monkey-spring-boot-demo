FROM harisekhon/ubuntu-java
VOLUME /tmp
COPY ./chaos-scripts /var/chaosscripts
RUN sh -c 'apt-get update'
RUN sh -c 'apt-get install iproute2 -qq'
RUN sh -c 'apt-get install net-tools -qq'
RUN sh -c 'apt-get install sudo -qq'
RUN useradd -ms /bin/bash chaos
