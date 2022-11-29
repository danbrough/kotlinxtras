#!/bin/bash
cd "$(dirname "$0")" 

./plugin/publish.sh $@

REPO=${1-M2}
echo publishing to $REPO
OPTS="${@:2}"

if [ "$REPO" == "SonaType" ]; then
	[ -z "$SONATYPE_REPO_ID" ] && echo SONATYPE_REPO_ID not set && exit 1
	OPTS=""$OPTS" -Dorg.gradle.unsafe.configuration-cache=false -PsignPublications -PpublishDocs"
fi

./gradlew "$OPTS" publishAllPublicationsTo${REPO}Repository