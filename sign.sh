#!/bin/sh
java -cp "classes:lib/*:conf" apo.tools.SignTransactionJSON $@
exit $?
