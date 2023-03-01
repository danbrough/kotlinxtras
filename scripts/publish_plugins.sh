#!/bin/bash
cd "$(dirname "$0")" && cd ..

REPO=${1-Xtras}
echo publishing to $REPO

./gradlew plugin:publishAllPublicationsToXtrasRepository

if [ "$REPO" == "SonaType" ]; then
   ./gradlew plugin:sonatypeOpen -PsonatypeDescription=xtras:plugin
	OPTS="-PsignPublications=true -PpublishDocs=true"
fi



#./gradlew -PpluginsOnly=true $OPTS plugin:publishAllPublicationsTo${REPO}Repository || exit 1
#  core:publishAllPublicationsTo${REPO}Repository || exit 1

#[ "$REPO" == "SonaType" ] &&  ./gradlew plugin:sonatypeClose




