#!/bin/bash
cd "$(dirname "$0")" 

REPO=${1-M2}
echo publishing to $REPO
export THANG=1234

if [ "$REPO" == "SonaType" ]; then
	[ -z "$SONATYPE_REPO_ID" ] && echo SONATYPE_REPO_ID not set && exit 1
	OPTS="-Dorg.gradle.unsafe.configuration-cache=false -PsignPublications=true -PpublishDocs=true"
fi


../gradlew "${@:2}" $OPTS publishAllPublicationsTo${REPO}Repository


