#!/bin/bash
cd "$(dirname "$0")" 


REPO=${1-Xtras}
echo publishing to $REPO

OPTS=""

if [ "$REPO" == "SonaType" ]; then
	#OPTS=""$OPTS" -Dorg.gradle.unsafe.configuration-cache=false -PsignPublications -PpublishDocs"
fi


./gradlew -Pplugins=true $OPTS publishAllPublicationsTo${REPO}Repository
./gradlew -Pplugins=true $OPTS publishAllPublicationsTo${REPO}Repository


