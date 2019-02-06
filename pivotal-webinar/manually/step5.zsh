#!/bin/zsh
clear
curl http://localhost:8090/fashion/bestseller | jq -C
echo
print -P "%F{blue}------------------------------------------------------------%f"
print -P "%F{blue}curl http://localhost:8090/fashion/bestseller%f"
