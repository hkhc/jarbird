#!/bin/sh
#
# Copyright (c) 2021. Herman Cheung
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
#

# Create a bootstrap version of the plugin, so that it can use itself to build complete set of artifacts and publish them.
PROJ_UTILS=jarbird-utils-bootstrap
PROJ_BASE=jarbird-bootstrap-base
PROJ_BOOTSTRAP=jarbird-bootstrap
./gradlew --stacktrace -c b2.settings.gradle.kts $1 $PROJ_UTILS:kotest $PROJ_BASE:kotest $PROJ_UTILS:publishToMavenLocal $PROJ_BASE:publishToMavenLocal $PROJ_BOOTSTRAP:publishToMavenLocal

