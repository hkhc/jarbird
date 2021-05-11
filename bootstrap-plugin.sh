#!/bin/sh
# Create a bootstrap version of the plugin, so that it can use itself to build complete set of artifacts and publish them.
PROJ_UTILS=jarbird-utils-bootstrap
PROJ_BASE=jarbird-bootstrap-base
PROJ_BOOTSTRAP=jarbird-bootstrap
./gradlew --stacktrace -c b2.settings.gradle.kts $1 $PROJ_UTILS:kotest $PROJ_BASE:kotest $PROJ_UTILS:publishToMavenLocal $PROJ_BASE:publishToMavenLocal $PROJ_BOOTSTRAP:publishToMavenLocal
