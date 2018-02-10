#!/usr/bin/env bash
clear
./gradlew :annotations:clean :annotations:assemble :annotations:generatePomFileForMavenPublication :annotations:publishToMavenLocal
./gradlew :runtime:clean :runtime:assembleRelease :runtime:generatePomFileForReleasePublication :runtime:publishReleasePublicationToMavenLocal
./gradlew :compiler:clean :compiler:assemble :compiler:generatePomFileForMavenPublication :compiler:publishToMavenLocal