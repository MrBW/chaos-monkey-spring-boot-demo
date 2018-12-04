#!/bin/zsh
clear
docker-compose --f ~/develop/chaos-monkey-spring-boot-demo/docker-compose.yml exec chaostoolkit /bin/bash
