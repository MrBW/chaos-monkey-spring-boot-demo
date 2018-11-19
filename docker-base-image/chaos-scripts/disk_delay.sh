#!/bin/sh
device='/dev/ram0'
#create a RAM disk
modprobe brd rd_nr=1 rd_size=131027
readdelay=100
writedelay=100
size=$(blockdev --getsize $device) # Size in 512-bytes sectors
mkfs.ext4 $device
echo "0 $size delay $device 0 $readdelay $device 0 $writedelay" | dmsetup create delay
