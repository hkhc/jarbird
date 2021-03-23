#!/bin/sh
# Create a bootstrap version of the plugin, so that it can use itself to build complete set of artifacts and publish them.
./gradlew --stacktrace -c b2.settings.gradle.kts $1 jarbird-bootstrap:kotest jarbird-bootstrap:publishToMavenLocal
