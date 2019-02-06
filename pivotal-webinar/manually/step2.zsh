#!/bin/zsh
clear
curl http://localhost:8090/actuator/chaosmonkey | jq -C
echo
print -P "%F{blue}------------------------------------------------------------%f"
print -P "%F{blue}curl http://localhost:8090/actuator/chaosmonkey%f"
