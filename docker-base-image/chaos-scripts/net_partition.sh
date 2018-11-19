#!/usr/bin/env bash
IFS=', ' read -r -a hosts <<< "$1"
iptables -I INPUT -s ${hosts[$2]} -j DROP
iptables -I OUTPUT -d ${hosts[$2]} -j DROP
