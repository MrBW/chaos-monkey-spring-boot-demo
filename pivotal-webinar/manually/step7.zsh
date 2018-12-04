#!/bin/zsh
clear
echo Request:
cat assault-config-exp.json
echo
echo Response:
curl -H "Content-Type: application/json" --data @assault-config-exp.json http://localhost:8090/actuator/chaosmonkey/assaults
echo
