#!/bin/bash
cd "$(dirname "$0")" && cd ..

./gradlew -Pinclude=libs publishAllPublicationsToXtras

