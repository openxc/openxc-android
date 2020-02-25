#!/usr/bin/env bash

openssl enc -d -aes256 -in secret.json.enc -out secret.json -pass pass:$1
pwd
ls
./gradlew bintrayUpload
./gradlew publishRelease
