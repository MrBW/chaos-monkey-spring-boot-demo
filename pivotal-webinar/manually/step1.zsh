#!/bin/zsh
clear
curl http://localhost:8080/startpage/legacy | jq -C
echo
print -P "%F{grey}------------------------------------------------------------%f"
print -P "%F{blue}curl http://localhost:8080/startpage/legacy%f"
