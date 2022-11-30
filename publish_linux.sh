#!/bin/bash
cd "$(dirname "$0")" 


REPO=${1-M2}
echo publishing to $REPO

OPTS=""

if [ "$REPO" == "SonaType" ]; then
	#OPTS=""$OPTS" -Dorg.gradle.unsafe.configuration-cache=false -PsignPublications -PpublishDocs"
fi


./gradlew $OPTS publishAllPublicationsTo${REPO}Repository


