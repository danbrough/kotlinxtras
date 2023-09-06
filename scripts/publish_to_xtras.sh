#!/bin/bash

cd `dirname $0` && cd ..

./gradlew plugin:publishAllPublicationsToXtras
./gradlew publishAllPublicationsToXtras -Pinclude=plugins

