#!/bin/bash

cd $(dirname "$0")

[ -z "${SONATYPE_REPO_ID}" ] && echo "SONATYPE_REPO_ID not set" && exit 1

./gradlew -PsignPublications -PpublishDocs publishAllPublicationsToSonaTypeRepository

