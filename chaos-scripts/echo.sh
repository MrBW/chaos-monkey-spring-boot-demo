#!/bin/bash
IFS=', ' read -r -a array <<< "$1"

echo "Narf ${array[$2]}"
