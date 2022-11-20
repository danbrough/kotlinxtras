#!/bin/bash

REPOSITORY=${1-SonaType}
echo publishing to $REPOSITORY

cd "$(dirname "$0")"
../gradlew -Dorg.gradle.unsafe.configuration-cache=false \
 publishAllPublicationsTo${REPOSITORY}Repository -PsignPublications
