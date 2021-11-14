#!/usr/bin/env bash
clear
./gradlew :annotations:publishAllPublicationsToMavenCentralRepository
./gradlew :runtime:publishAllPublicationsToMavenCentralRepository
./gradlew :runtime-androidx:publishAllPublicationsToMavenCentralRepository
./gradlew :runtime-kt:publishAllPublicationsToMavenCentralRepository
./gradlew :compiler:publishAllPublicationsToMavenCentralRepository
./gradlew :compiler-ksp:publishAllPublicationsToMavenCentralRepository