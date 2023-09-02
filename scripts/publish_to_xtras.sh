#!/bin/bash

cd `dirname $0` && cd ..

./gradlew publishAllPublicationsToXtras -Ppublish=plugin
./gradlew publishAllPublicationsToXtras -Ppublish=core
