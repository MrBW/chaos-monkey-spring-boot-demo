#!/usr/bin/env bash
set -o nounset
set -o errexit

if [[ $# -eq 0 ]] || [[ $# -eq 1  && $1=="--help-chaos" ]]
  then
    printf "net_delay <dev> <delay-in-ms>: Add a delay <delay-in-ms> to device <dev> for each packet."
  exit 0
fi
#Config using FIFO adding a fixed delay of 100ms
tc qdisc add dev $1 root netem delay $2
