#!/bin/zsh
clear
curl -X POST http://localhost:8090/actuator/chaosmonkey/enable
echo
print -P "%F{blue}------------------------------------------------------------%f"
print -P "%F{blue}curl -X POST http://localhost:8090/actuator/chaosmonkey/enable%f"
