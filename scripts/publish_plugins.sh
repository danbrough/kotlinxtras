#!/bin/bash
cd "$(dirname "$0")" && cd ..

REPO=${1-Xtras}
echo publishing to $REPO

OPTS="-PpluginsOnly=true"

if [ "$REPO" == "SonaType" ]; then
   ./gradlew plugin:sonatypeOpen -PsonatypeDescription=xtras:plugin
	OPTS="-PsignPublications=true -PpublishDocs=true"
fi

./gradlew $OPTS plugin:publishAllPublicationsTo${REPO}Repository || exit 1
./gradlew $OPTS core:publishAllPublicationsTo${REPO}Repository || exit 1

