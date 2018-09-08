FROM ubuntu:18.04
COPY install-chaos.sh /root

# Install.
SHELL ["/bin/bash", "-c"]
RUN \
  sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list && \
  apt-get update && \
  apt-get -y upgrade && \
  apt-get -y install apt-utils

RUN chmod a+x /root/install-chaos.sh
RUN . /root/install-chaos.sh

# Set environment variables.
ENV HOME /root

# Define working directory.
WORKDIR /root

# Define default command.
CMD ["bash"]
