#!/bin/bash


cd `dirname $0` && cd ..

if [ "$1" == "migrate" ]; then
  ./gradlew refreshVersionsMigrate
fi

./gradlew refreshVersions


git diff versions.properties

