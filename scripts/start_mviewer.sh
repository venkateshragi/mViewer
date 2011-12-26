#!/bin/bash
if [ $1 != "" ]
then
   port=$1
else
   . mViewer.properties
   port=$WINSTONE_PORT
fi
java -jar ./winstone-0.9.10.jar --httpPort=$port --ajp13Port=-1 --warfile=./mViewer.war
