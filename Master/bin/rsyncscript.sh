#!/usr/bin/expect -f
set timeout 60
# connect via scp
spawn ssh "pi@10.10.10.105"
expect "password:"
send "raspberry\r";
interact
spawn rsync -av /home/stu1/s790/pad2039/workspace2/Slave/src/ pi@10.10.10.105:/home/pi/Group5Slave
expect "password:"
send "raspberry\r";
interact
spawn ssh "pi@10.10.10.105"
expect "password:"
send "raspberry\r";
send "cd Group5Slave\r"
send "sh remote.sh\r"
interact
