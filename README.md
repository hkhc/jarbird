[![Maven Central](https://img.shields.io/maven-central/v/io.hkhc.gradle/jarbird.svg)](https://search.maven.org/artifact/io.hkhc.gradle/jarbird)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/hkhc/jarbird/io.hkhc.jarbird.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=gradlePluginPortal)](https://plugins.gradle.org/plugin/io.hkhc.jarbird)

# Jarbird - Get back the fun of component publishing

- Build, Sign, Source archives, Doc archives, POM setup in unified way.
- Supports Maven Central, custom Maven repository managers, Artifactory, Gradle Plugin Portal. (and of course Maven Local)
- Eat our own dog food. This plugin is published by _Jarbird_ itself.
- [TODO] Automatic Sonatype component release.

## Further information

- [Full documentation](https://hkhc.github.io/jarbird/docs/intro)
- [Tutorials](https://hkhc.github.io/jarbird/docs/tutorials/index)
- [References](https://hkhc.github.io/jarbird/docs/reference/index)
- [Source code](https://github.com/hkhc/jarbird)
- [Sample code](https://github.com/hkhc/jarbird-samples)

## Features at a glance

### Declare you POM at `pom.yaml`

```yaml
group: jarbirdsamples
artifactId: mavencentraldemo
version: 1.0
packaging: jar

description: Demo to publish simple library to Maven Central

licenses:
  - name: Apache-2.0
    dist: repo

developers:
  - id: demo
    name: Jarbird Demo
    email: jarbird.demo@fake-email.com

scm:
  repoType: github.com
  repoName: hkhc/jarbird-samples/mavencentral
```

### Specify your credentials at `gradle.properties`

...or environment variables

```properties
signing.gnupg.keyName=[Key ID from GNuPG] 
signing.gnupg.passphrase=[Passphrase of GNUPG keybox]
repository.mavencentral.username=[OSS sonatype server username]
repository.mavencentral.password=[OSS sonatype server password]
# true if the account is created after Feb 2021
repository.mavencentral.newUser=true
```

### Declare the plugin

```kotlin
plugins {
    id("io.hkhc.jarbird") version "0.7.0"        
}
// ...
jarbird {
    mavenCentral()
    pub {}
}
```

### Publish your component

```text
$ ./gradlew jbPublish
```

## Components publishing is far too complicated

Publishing libraries to public repositories like Maven, Bintray are not really
difficult at all, but it is a nuisance, especially for first-timer. Setting up
accounts is a lengthy process, and we need to set our build script right so that
those repositories accept our submission. By "right", we mean using proper plugins,
declaring proper publication, and executing proper Gradle tasks. Once we have set
up all those things, it is mostly "just work". So there is a significant fiction
to make our components available in repositories rather than copy-and-paste among projects.

<p><u>Worst still</u>, the build script code to publish artefacts to Maven is quite 
different from the script for Bintray repository. We need a double effort if we want 
to publish to both repositories. It is possible to publish components to Maven Central 
by Bintray server, but that means vendor lock up.</p>

The complete Gradle script to publish component is filled with boilerplate code.
It is hard to read and the project-specific details are buried in the jungle of
tasks and configurations.

“Jarbird” Gradle plugin is designed to make these things simple. Simply speaking,
this plugin wraps up the boilerplate code to publish components to major public
repositories, including Maven Central, Bintray/JCenter, Gradle’s own plugin repository.
Its aim is making simple thing simple, so undoubtedly there are things that can be
done by hand-coding the publishing code in Gradle build script, but cannot be done
by this plugin. However, if things are really complicated, they deserve effort to
do it in the good old way.

# What's new

See [RELEASE.md](RELEASE.md)
