#!/bin/bash

cd "$(dirname "$0")"

OPTS="-Dorg.gradle.unsafe.configuration-cache=false -PsignPublications -PpublishDocs"
./gradlew "$OPTS"
