#!/bin/zsh
clear
echo Request:
cat assault-config-exception.json
echo
echo Response:
curl -H "Content-Type: application/json" --data @assault-config-exception.json http://localhost:$1/actuator/chaosmonkey/assaults
echo