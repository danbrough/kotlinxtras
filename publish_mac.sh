#!/bin/bash

cd "$(dirname "$0")"


./gradlew -Dorg.gradle.unsafe.configuration-cache=false -PsignPublications -PpublishDocs \
  `cat mac_targets.txt`
