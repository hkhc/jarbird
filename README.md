[![jCenter](https://img.shields.io/maven-metadata/v/https/jcenter.bintray.com/io/hkhc/gradle/jarbird/maven-metadata.xml.svg?label=jCenter)](https://bintray.com/hermancheung/maven/io.hkhc.gradle:jarbird/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/io.hkhc.gradle/jarbird.svg)](https://search.maven.org/artifact/io.hkhc.gradle/jarbird)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/hkhc/jarbird/io.hkhc.jarbird.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=gradlePluginPortal)](https://plugins.gradle.org/plugin/io.hkhc.jarbird)

# [Document to be revamped]

# Jarbird Gradle Plugin

## Introduction

Publishing libraries to public repositories like Maven, Bintray are not really difficult at all, but it is a nuisance, especially for first-timer. Setting up accounts is a lengthy process, and we need to set our build script right so that those repositories accept our submission. By "right" we mean using proper plugins, declaring proper publication, and executing proper tasks. Once we have set up all those things, it is mostly "just work". So there is a significant "fiction" to make our components available in repositories rather than copy-and-paste among projects.

Worst still, the code to publish artefacts to maven is quite different from that for Bintray repository. We need a double effort if we want to publish to both repositories.

The result Gradle script is filled with boilerplate code. It is hard to read and the project-specific details are buried in the jungle of tasks and configurations.

“Jarbird” Gradle plugin is designed to make these things simple. Simply speaking, this plugin wraps up the boilerplate code to publish components to major public repositories, including Maven Central, Bintray/JCenter, Gradle’s own plugin repository. Its aim is making simple thing simple, so undoubtedly there are things that can be done by hand-coding the publishing code in Gradle build script, but cannot be done by this plugin. However, if things are really complicated, they deserve effort to do it in the good old way.

## You need the following before you start

You need quite amount of preparation to publish your first component. Most of them are one-off that you may use them for publishing an unlimited number of components. A few others are per component settings that you need to have it for each publication.

### One-time setup:

#### Bintray

- Create Bintray account (if you want to publish to jCenter), so that you have username and password of the account.

#### Maven Central

- Sonatype account (if you want to publish to Maven Central), so that you have username and password of the account
- A GPG key that identifies yourself, and digitally signing your component. (It is optional for jCenter but mandatory for Maven Central), so that you have keybox (.kbx) file, passphrase to unlock it, and a key name
- Apply for your own group name. The simplest way is to use `io.github.username` as your group ID. Otherwise, You may need to have full control of an internet domain name, and have the right to update the TXT record of your domain to get through the procedure. One group name can be used by multiple components, and of course, nothing stops you from creating one group ID for each of your projects.

#### Gradle Plugin Repository

- Gradle plugin repository account (if you want to publish your own Gradle plugin so that people can use the new syntax to apply your plugin), so that you have "key" and "secret" of your account.

### Per component setup:

#### Git

- Publish our code to a public repository like Github, Gitlab, BitBucket, etc, so that you have an URL of your code. You need an URL to the repo.
- Of course, you are not forced to use git, but this component is designed with git repository in mind, so it is simpler to do so.

#### The POM model details

- You need some details about your components so that they are submitted to repositories along with your components. This information includes group ID, artefact name, version, you or your organization information, your code repository information, etc.

## Installation

New style with Kotlin script:

```kotlin
plugins {
    id("io.hkhc.jarbird") 
}
```

New style with Groovy script

```groovy
plugins {
    id "io.hkhc.jarbird"
}
```

Old style with Kotlin script:

```kotlin
buildscript {
    // either one of mavenCentral or jcenter. The plugin is deployed to both repositories.
    mavenCentral()
    // jcenter()
    classpath("io.hkhc.gradle:jarbird:0.2")
}

apply(plugin = "io.hkhc.jarbird")
```

Old style with Groovy script

```groovy
buildscript {
    // either one of mavenCentral or jcenter. The plugin is deployed to both repositories.
    mavenCentral()
    // jcenter()
    classpath "io.hkhc.gradle:jarbird:0.2"
}

apply plugin "io.hkhc.jarbird"
```

## Tutorial

- fork the sample repo
- provide group name
- Simplest Hello World
  - `io.github.hkhc:helloworld:1.0-SNAPSHOT` 
  - maven local
- Add dokka
- Add signing info
- `pom.yml`
- maven central
- jcenter
- android project
- gradle plugin project
- 

## Configuration

Given we have the settings above in place, we need group ID and version only for the simplest scenario to publish artefacts to Maven local repository.

```kotlin
group = "io.github.myusername"
version = "1.0-SNAPSHOT"
```

The plugin creates the `publishing` configuration, the corresponding `publication` and a number of tasks with prefix `jb` and under the group "Jarbird Publishing".

| Task                         | Description                                                                     |
| ---------------------------- | ------------------------------------------------------------------------------- |
| `jbPublish`                  | Publish Maven publication 'Lib' to Maven Local, 'Mavenlib' Repository, Bintray. |
| `jbPublishToBintray`         | Publish Maven publication 'lib' to Bintray                                      |
| `jbPublishToMavenLocal`      | Publish Maven publication 'lib' to the local Maven Repository                   |
| `jbPublishToMavenRepository` | Publish Maven publication 'lib' to the 'MavenLib' Repository                    |

So we may just issue the command to publish artefacts to Maven local repository. That should include the jar file of the library, the jar file of source code archive, and a minimal POM file for the artefacts.

```bash
./gradlew jbPublishToMavenLocal
```

One can check the published files under  `~/.m2/repository/io/github/myusername/1.0-SNAPSHOT` directory.

As a side note, for other Gradle project that makes use of the artefacts in Maven local repository, it should have the following snippet at the `buildscript` block:

```kotlin
buildscript {
    repositories {
        // the following is needed for Maven Local repository
        mavenLocal() 
        ... // other repositoeries
    }
}
```

You may notice that there are two warnings during publishing with such minimal configuration like this:

```
WARNING: [jarbird] No signing setting is provided. Signing operation is ignored. Maven Central publishing cannot be done without signing the artifacts.
```

It would be technically fine for a locally used component without documentation archive and signature of artefacts. But they are mandatory for Maven Central publishing.

## Configuration in build script

We may add Gradle extension `simplyPublish` to configure what to do with the plugin. 

```kotlin
simplyPublish {
    // our customization here.
}
```

## Signing configuration

We set up the singing details in gradle.properties as specify in Gradle [doc]([The Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)). Gradle signing plugin provides a few ways to configure how artefacts are signed.

- v1 in `gradle.properties`
  
  ```
  signing.keyId=24875D73
  signing.password=secret
  signing.secretKeyRingFile=/Users/me/.gnupg/secring.gpg
  ```

- v2 in `gradle.properties`
  
  ```
  signing.gnupg.executable=gpg
  signing.gnupg.useLegacyGpg=true
  signing.gnupg.homeDir=gnupg-home
  signing.gnupg.optionsFile=gnupg-home/gpg.conf
  signing.gnupg.keyName=24875D73
  signing.gnupg.passphrase=gradle
  ```

- In build script with ascii-armored key
  
  ```
  useInMemoryPgpKeys(key, password)
  ```
  
  We could add the `useGpg` flag to determine which way we configure the keys.
  
  ```kotlin
  simplyPublish {
      useGpg = true // true for v2, and false for v1
  }
  ```
  
  The plugin try to examine the setting in `gradle.properties` if one of the configuration types is available and it does not match what we set in `simplyPlugin` block, the plugin overrides it and show a warning like this:
  
  ```
  WARNING: [jarbird] Setting to use gpg keyring file but signing gpg keybox  configuration is found. Switch to use gpg keybox
  ```
  
  if we have `--info` option in Gradle command line.
  
  If signing information is incomplete, the signing operation is ignored and we get the following warning
  
  ```
  WARNING: [jarbird] No signing setting is provided. Signing operation is ignored. Maven Central publishing cannot be done without signing the artifacts.
  ```

## POM Declaration

### Simplest case

In simplest form to deploy artefacts to the local Maven repository, we don't need any settings beyond `group` and `version` in build script. For example,

```
group = "mygroup"
version = "1.0"
```

The artefact ID will be the project name if we don't specify one. The plugin generates the necessary `publishing` block and `publication` block that we used to create them manually. 

So we can just start building our artefacts with this.

```
./gradlew jbPublishToMavenLocal
```

and we can find the component we build at `~/.m2/repository`

To make use the component in another project, just add `mavenLocal()`to `repositories` block

```
repositories {
    mavenLocal()
    // and other repositories that you have already specified
    mavenCentral()
    jcenter()
}
```

### More Complete Case - YML declaration

To publish it to public repository like Maven Central, we need to provide more information in the form of [POM XML](https://maven.apache.org/pom.html). Traditionally we provide this POM information in hand-coded publishing section. However to make things more complicated, when we publish our artefacts to multiple repositories, like Maven Central, jCenter, and Gradle plugin portal, we need to specify the same piece of information multiple times at different plugin configurations. The published POM XML is generated by these plugins. This creates a significant amount of boilerplate code in the build script. Simple publisher is aimed to simplify this process by having a single place to specify POM information.

Simple Publisher read POM information from a file called pom.yml. It is YAML format means to be human friendly to specify POM details.

The plugin read pom.yml at several places. The plugin read pom.yml at the following order:

- `$GRADLE_USER_HOME/pom.yml`

- `[project-root-dir]/pom.yml`

- `[sub-project-dir]/pom.yml`

- file specified by `pomFile` system property. 

The rule of thumb is that the files will be sitting side-by-side with gradle.properties. By putting some of the information out of the project directory, some common information can be shared among projects, like developer information.

The content of later files overwrites the content of the preceding files. The content of the files will be used to generate the configuration of maven publication, bintray publication, and gradle plugin portal.

### Format of pom.yml

Most of the content within pom.yml could be optional. However, it takesa full pom.yml file to satisfy Maven Central requirement.

This is the pom.yml from the Simple Publisher source code. Yes, it makes use of itself to build and publish artifact.

```yaml
# group name of the artifact, use project.group if omitted
group: io.hkhc.gradle
artifactId: jarbird
name: Simple Publisher
version: 0.3.3.2
description: Wrapping boilerplate code of publishing artefact to maven repository
packaging: jar

licenses:
- name: Apache-2.0

developers:
- id: hkhc
  name: Herman Cheung
  email: herman.kh.cheung+jarbird@gmail.com

scm:
  repoType: github.com
  repoName: hkhc/jarbird

plugin:
  id: io.hkhc.jarbird
  displayName: Jarbird Plugin
  implementationClass: io.hkhc.gradle.JarbirdPlugin
  tags:
  - publish
```

### Coordinate

The combination `group:artifactId:version` is known as the "coordinate" of the artefact. if pom.yml is not provided, the `project.group`, `project.name` and `project.version` is used to form the coordinate.

Otherwise, the `group`, `artifactId`, `version` in pom.yml will be used.

If both values in the build script and pom.yml exist, the value at pom.xml has precedence. 

When publishing to Maven Central, make sure the group name has been properly [verified](https://central.sonatype.org/pages/producers.html#individual-projects-open-source-software-repository-hosting-ossrh).

### Artifact name

`name`  field is a descriptive name of the artefact. If it is not specified, `group:artifactId` will be used as the name.

### License

We can specify multiple licenses for the artefact. Most of the time we need only one. In its simplest form, we may just specify the `name` field under `licenses`. For example

```yaml
licenses:
- name: Apache-2.0
```

 Then URL is automatically provided for configure publishing plugins. Optionally we may provide more information:

```yaml
licenses:
- name: Apache-2.0
  url: http://www.apache.org/licenses/LICENSE-2.0.txt
  dist: repo
  comments: This is an example of license block
```

Right now only the following license names are automatically expanded:

- `Apache-2.0`

- `BSD-3-Clause`

- `MIT`

- `GPLv3`

- `LGPLv3`

When merging license information from multiple pom.yml, the following rules apply:

- License information of different name is treated as separate licenses in merged information.

- License information at pom.xml of higher precedence overwrites the license of the same name in the file of lower precedence, in field by field basis.

### Developers and Contributors

We can specify multiple developers and contributors in pom.yml. Both of them share the same structure. Minimally, one developer entry should be provided which contains `name`, `email`.  In full, it accepts the following fields:

```yaml
developers:
- id: <id>
  name: <your name>
  email: <your email>
  organization: <your organization>
  organizationUrl: <url of your organization>
  timezone: <time zone of your place>
```

### Source Control information

We need to provide quite a bit of information for your source code repository. Simple publisher provide a shorthand representation to simply the process:

```yaml
Scm:
  repoType: <the hostname of the repo>
  repoName: <the name of your repo>
```

This will expand to fields in POM XML. For example,

```yaml
Scm:
  repoType: github.com
  repoName: myname/myrepo
```

will expand to:

| field               | value                                     |
| ------------------- | ----------------------------------------- |
| url                 | `https://github.com/myname/myrepo`        |
| connection          | `scm:git@github.com/myname/myrepo`        |
| developerConnection | `scm:git@github.com/myname/myrepo.git`    |
| issueType           | `github.com`                              |
| issueUrl            | `https://github.com/myname/myrepo/issues` |

If `repoType` or `repoName` are not specified, no automatic generation is done.  One may manually fill in the information at pom.yml. 

### Web

We may specify the detail of Web section in pom.yml:

```yaml
web:
  url: https://myproject.org
  description: This is my library project.
```

If they are omitted, the `url` field is replaced by the value of `scm.url`, and the description field is replaced by the description of the artifact.

### Url

The project has its url field. If it is omitted, the Scm.url field is used. 

## Minimal requirement of POM for Maven Central

While the information in pom.yml is mostly optional, we need them all such that Maven Central repository accepts our submission.

## Publishing Gradle Plugin

Simple Publisher can also help build and publish Gradle plugin to Gradle Plugin Portal and other repositories. We need some more information about the plugin. Add the following section to pom.xml

```yaml
plugin:
  id: <your "new style" plugin ID>
  displayName: <Descriptive name of the plugin>
  implementationClass: <Implementation class of the plugin>
  tags: 
    - <tags to be shown in Gradle Plugin Portal page>
```

Then add the following to Gradle script:

```kotlin
simplyPublish {
    gradlePlugin = true
}
```

Of course, we still need the Gradle Plugin Portal account setting in `gradle.properties`, which is be available at your profile page at Gradle Plugin Portal. 

```
gradle.publish.key=API Key
gradle.publish.secret=API Secret
```

When all of these are presents, the plugin adds a task `jbPublishToGradlePortal`  that you can publish the plugin to Gradle Portal.

Gradle find the location of the local repository in the following order:

- System property `maven.repo.local`

- The `<localRepository>` maven [setting](http://maven.apache.org/settings.html#Settings_Details)
  
  - at `$M2_HOME/conf/setting.xml` where `M2_HOME` is an environment variable. 
  
  - at `~/.m2/conf/settings.xml`

- If all of above are not found, the default is `~/.m2/repository`
