#!/bin/bash
. mViewer.properties
port=${1:-$WINSTONE_PORT}
echo Using Http Port : $port
java -jar ./winstone-0.9.10.jar --httpPort=$port --ajp13Port=-1 --warfile=./mViewer.war
