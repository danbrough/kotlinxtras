#!/bin/bash

cd `dirname $0` && cd ..

./gradlew publishAllPublicationsToXtras -Pinclude=plugin
./gradlew publishAllPublicationsToXtras -Pinclude=plugins

