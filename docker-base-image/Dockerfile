FROM harisekhon/ubuntu-java
LABEL application="Chaos Shopping Demo"
LABEL student=${STUDENT}
VOLUME /tmp
COPY ./chaos-scripts /var/chaosscripts
COPY .bashrc root/
RUN apt-get update && apt-get install -y --no-install-recommends apt-utils
RUN sh -c 'apt-get update'
RUN sh -c 'apt-get install iproute2 -qq'
RUN sh -c 'apt-get install net-tools -qq'
RUN sh -c 'apt-get install sudo -qq'
RUN sh -c 'apt-get install iputils-ping -qq'
RUN sh -c 'apt-get install apache2-utils -qq'
RUN sh -c 'apt-get install nano -qq'
RUN sh -c 'apt-get install curl -qq'
RUN sh -c 'apt-get install stress -qq'
RUN useradd -ms /bin/bash chaos
