#!/bin/bash
cd "$(dirname "$0")" 

REPO=${1-M2}
echo publishing to $REPO

[ "$REPO" == "SonaType" ] && OPTS="-Dorg.gradle.unsafe.configuration-cache=false -PsignPublications -PgenerateDocs"

cd "$(dirname "$0")"
../gradlew $OPTS  publishAllPublicationsTo${REPO}Repository $OPTS 
