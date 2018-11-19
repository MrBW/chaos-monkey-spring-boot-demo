#!/usr/bin/env bash
set -o nounset
set -o errexit

if [[ $# -eq 0 ]] || [[ $# -eq 1  && $1=="--help-chaos" ]]
  then
    printf "net_delay_rate_limit <dev>: Add a token bucket with a size of 900 bytes to the device."
  exit 0
fi

#Config using token bucket filter, bucket size 900 bytes
tc qdisc add dev $2 root tbf rate 16kbit latency 20ms burst 900
