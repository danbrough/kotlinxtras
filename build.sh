#!/bin/bash

cd $(dirname "$0")

#REPO="Sonatype"
REPO="M2"

if [ ! -z "$1" ]; then
	REPO="$1"
fi

if [ "$(uname)" = "Darwin" ]; then
  ./gradlew -PsignPublications -PpublishDocs `cat mac_targets.txt`
  exit 0
fi

./gradlew -PsignPublications -PpublishDocs publishAllPublicationsTo${REPO}Repository

