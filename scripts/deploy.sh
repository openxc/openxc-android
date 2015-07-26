#!/usr/bin/env bash

./gradlew bintrayUpload
./gradlew publishRelease
./scripts/updatedocs.sh
