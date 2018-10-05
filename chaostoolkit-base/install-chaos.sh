#!/bin/bash
echo C.UTF exports
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
echo Install python3
apt-get -y install python3 python3-venv
echo Install pkill openssl
apt-get -y install procps
apt-get -y install openssl
echo Create a virtual environment
python3 -m venv ~/.venvs/chaostk
source  ~/.venvs/chaostk/bin/activate
pip install wheel
echo Install Chaos Toolkit
pip install chaostoolkit
echo Check Chaos Toolkit version
chaos --version
echo Install driver Spring Boot
pip install -U chaostoolkit-spring
echo Install ChaosToolkit plugin chaoshub
pip install chaostoolkit-chaoshub
