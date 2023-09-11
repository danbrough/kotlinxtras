#!/bin/bash
START=1
END=$1
echo "Countdown"

for (( c=$START; c<=$END; c++ ))
do
    echo "C: $c"
    sleep 1
done

echo
echo "Boom!"