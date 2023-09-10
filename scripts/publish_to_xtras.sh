#!/bin/bash

cd `dirname $0` && cd ..

./gradlew plugin:publishAllPublicationsToXtras -Pinclude=plugin
./gradlew publishAllPublicationsToXtras -Pinclude=plugins

