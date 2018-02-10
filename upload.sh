#!/usr/bin/env bash
clear
./gradlew :annotations:clean :annotations:assemble :annotations:generatePomFileForMavenPublication :annotations:bintrayUpload
./gradlew :runtime:clean :runtime:assembleRelease :runtime:generatePomFileForReleasePublication :runtime:bintrayUpload
./gradlew :compiler:clean :compiler:assemble :compiler:generatePomFileForMavenPublication :compiler:bintrayUpload