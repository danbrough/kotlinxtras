#!/bin/bash

cd "$(dirname "$0")" && cd ..

OPTS="-Dorg.gradle.unsafe.configuration-cache=false -PsignPublications -PpublishDocs"
./gradlew "$OPTS"
