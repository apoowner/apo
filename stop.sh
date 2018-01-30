#!/bin/sh
if [ -e ~/.apo/apo.pid ]; then
    PID=`cat ~/.apo/apo.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ~/.apo/apo.pid` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ~/.apo/apo.pid
    echo "Apo server stopped"
fi

