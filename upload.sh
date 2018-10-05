#!/usr/bin/env bash
clear
./gradlew :annotations:clean :annotations:assemble :annotations:generatePomFileForMavenPublication :annotations:bintrayUpload
./gradlew :runtime:clean :runtime:assembleRelease :runtime:generatePomFileForReleasePublication :runtime:bintrayUpload
./gradlew :runtime-kt:clean :runtime-kt:assembleRelease :runtime-kt:generatePomFileForReleasePublication :runtime-kt:bintrayUpload
./gradlew :compiler:clean :compiler:assemble :compiler:generatePomFileForMavenPublication :compiler:bintrayUpload