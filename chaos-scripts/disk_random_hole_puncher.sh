#!/bin/bash
start_sector=0
good_sector_size=0

for sector in {0..1048576}; do

    if [[ ${RANDOM} == 0 ]]; then
        echo "${start_sector} ${good_sector_size} linear /dev/loop0 ${start_sector}"
        echo "${sector} 1 error"
        start_sector=$((${sector}+1))
        good_sector_size=0
    else
        good_sector_size=$((${good_sector_size}+1))
    fi
done

echo "${start_sector} $((${good_sector_size}-1)) linear /dev/loop0 ${start_sector}"

