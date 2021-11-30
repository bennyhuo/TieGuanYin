#!/usr/bin/env bash
clear
./gradlew :annotations:publishToMavenLocal
./gradlew :runtime:publishToMavenLocal
./gradlew :runtime-androidx:publishToMavenLocal
./gradlew :runtime-kt:publishToMavenLocal
./gradlew :compiler:publishToMavenLocal
./gradlew :compiler-ksp:publishToMavenLocal