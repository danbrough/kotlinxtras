#!/bin/bash

cd $(dirname "$0")

REPO="Sonatype"

if [ ! -z "$1" ]; then
	REPO="$1"
fi

if [ "$(uname)" = "Darwin" ]; then
  ./gradlew -PsignPublications `cat mac_targets.txt`
  exit 0
fi

./gradlew -PsignPublications  publishAllPublicationsTo${REPO}Repository

