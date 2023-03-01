#!/bin/bash
cd "$(dirname "$0")" && cd ..

DESCRIPTION=${1-"xtras:plugin"}

#./gradlew plugin:publishAllPublicationsToXtrasRepository

./gradlew -PsonatypeDescription="$DESCRIPTION" plugin:sonatypeOpen


#./gradlew -PpluginsOnly=true $OPTS plugin:publishAllPublicationsTo${REPO}Repository || exit 1
#  core:publishAllPublicationsTo${REPO}Repository || exit 1

#[ "$REPO" == "SonaType" ] &&  ./gradlew plugin:sonatypeClose



