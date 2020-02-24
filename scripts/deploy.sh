#!/usr/bin/env bash

./gradlew bintrayUpload
openssl enc -d -aes256 -in secret.json.enc -out secret.json -pass pass:$secret
./gradlew publishRelease
