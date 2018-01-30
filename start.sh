#!/bin/sh
if [ -e ~/.apo/apo.pid ]; then
    PID=`cat ~/.apo/apo.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "Apo server already running"
        exit 1
    fi
fi
mkdir -p ~/.apo/
DIR=`dirname "$0"`
cd "${DIR}"
if [ -x jre/bin/java ]; then
    JAVA=./jre/bin/java
else
    JAVA=java
fi
nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/* -Dapo.runtime.mode=desktop apo.Apo > /dev/null 2>&1 &
echo $! > ~/.apo/apo.pid
cd - > /dev/null
