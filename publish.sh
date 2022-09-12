#!/bin/bash

cd $(dirname "$0")

if [ "$(uname)" = "Darwin" ]; then
  ./gradlew -PsignPublications `cat mac_targets.txt`
  exit 0
fi

./gradlew -PsignPublications  publishAllPublicationsToSonatypeRepository

