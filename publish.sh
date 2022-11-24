#!/bin/bash
cd "$(dirname "$0")" 

./plugin/publish.sh $@

REPO=${1-M2}
echo publishing to $REPO
OPTS="${@:2}"

[ "$REPO" == "SonaType" ] && OPTS="$OPTS -Dorg.gradle.unsafe.configuration-cache=false -PsignPublications -PpublishDocs"

./gradlew $OPTS publishAllPublicationsTo${REPO}Repository


