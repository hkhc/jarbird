#!/bin/sh
# Create a bootstrap version of the plugin, so that it can use itself to build complete set of artifacts and publish them.
./gradlew -c b2.settings.gradle.kts publishToMavenLocal

