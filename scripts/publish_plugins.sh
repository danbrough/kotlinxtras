#!/bin/bash
cd "$(dirname "$0")" && cd ..

REPO=${1-Xtras}
echo publishing to $REPO



if [ "$REPO" == "SonaType" ]; then
	OPTS="-PsignPublications=true -PpublishDocs=true"
fi

echo OPTS $OPTS

./gradlew -PpluginsOnly=true $OPTS plugin:publishAllPublicationsTo${REPO}Repository \
  plugins:publishAllPublicationsTo${REPO}Repository


