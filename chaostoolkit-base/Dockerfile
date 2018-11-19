FROM ubuntu:18.04
# BUG: Getting tons of debconf messages unless TERM is set to linux
ENV TERM linux

COPY install-chaos.sh /root
COPY config/.bashrc /root
COPY config/settings.yaml /root/.chaostoolkit/settings.yaml

ADD chaostoolkit-experiments/ /root

# Install.
SHELL ["/bin/bash", "-c"]
RUN \
  sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list && \
  apt-get update && \
  apt-get -y install apt-utils && \
  apt-get -y upgrade && \
  apt-get -y install nano && \
  apt-get -y install inetutils-ping && \
  apt-get -y install curl

RUN chmod a+x /root/install-chaos.sh
RUN . /root/install-chaos.sh

# Set environment variables.
ENV HOME /root
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

# Define working directory.
WORKDIR /root

# Define default command.
CMD ["bash"]
