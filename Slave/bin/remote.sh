#!/bin/bash
javac *.java
java Slave

while true;
do
	if ["$(pidof java)"];
	then 
		echo "Running"
	else
		echo "Not Running"
		java Slave
	fi
done
