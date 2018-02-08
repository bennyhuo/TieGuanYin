#!/usr/bin/env bash
clear
./gradlew :annotations:assemble :annotations:generatePomFileForMavenPublication :annotations:bintrayUpload
./gradlew :runtime:assembleRelease :runtime:generatePomFileForReleasePublication :runtime:bintrayUpload
./gradlew :compiler:assemble :compiler:generatePomFileForMavenPublication :compiler:bintrayUpload