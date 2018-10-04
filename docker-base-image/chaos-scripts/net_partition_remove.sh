#!/usr/bin/env bash
IFS=', ' read -r -a hosts <<< "$1"
iptables -D INPUT -s ${hosts[$2]} -j DROP
iptables -D OUTPUT -d ${hosts[$2]} -j DROP
