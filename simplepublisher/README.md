[![jCenter](https://api.bintray.com/packages/hermancheung/maven/simplepublisher/images/download.svg) ](https://bintray.com/hermancheung/maven/simplepublisher/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.hkhc.gradle/simplepublisher/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.hkhc.gradle/simplepublisher)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/hkhc/simplepublisher/io.hkhc.simplepublisher.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=gradlePluginPortal)](https://plugins.gradle.org/plugin/io.hkhc.simplepublisher)


# SimplePublisher Gradle Plugin

## Introduction

Publishing libraries to public repositories like Maven, Bintray are not really difficult at all, but it is a nuisance, especially for first-timer. Setting up accounts are lengthy process, and we need to set our build script right so that those repositories accept our submission. By "right" we means using proper plugin, declaring proper publication, and executing proper tasks. Once we have set up all those things, it is mostly "just work". So there is a significant "fiction" to make our components available in repositories rather than copy-and-paste among projects.

Worst still, the code to publish artifacts to maven is quite different from that for Bintray repository. We need double effort if we want to publish to both repository. 

The result Gradle script is filled with boilerplate code. It is hard to read and the project specific details is buried in the jungle of tasks and configurations.

"SimplePublisher" Gradle plugin is designed to make these things simple. Simply speaking, this plugin wrap up the boilerplate code to publish components to major public repositories, including Maven Central, Bintray/JCenter, Gradle's own plugin repository. Its aim is making simple thing simple, so undoubtedly there are things that can be done by hand-code the publishing code in Gradle build script, but cannot be done by this plugin. However, if thing is really complicate, it deserve effort to do it in good old way.

## You need the following before you start

You need quite amount of preparation to publish your first component. Most of them are one off that you may use them for publishing unlimited number of components. A few others are per component settings that you need to have it for each publication.

### One time setup:

#### Bintray

- Create Bintray account (if you want to publish to jCenter), so that you have username and password of the account.

#### Maven Central

- Sonatype account (if you want to publish to Maven Central), so that you have username and password of the account
- A GPG key that identify yourself, and digitally signing your component.  (It is optional for jCenter, but mandatory for Maven Central), so that you have keybox (.kbx) file, passphrase to unlock it, and a key name
- Apply for your own group name. The simplest way is to use `io.github.username` as your group ID. Otherwise, You may need to have full control of an internet domain name, and have right to update the TXT record of your domain  to get though the procedure. One group name can be used by multiple components, and of course nothing stop you from create one group ID for each of your project.

#### Gradle Plugin Repository

- Gradle plugin repository account (if you want to publish your own Gradle plugin so that people can use the new syntax to apply your plugin), so that you have "key" and "secret" of your account.

### Per component setup:

#### Git

- Publish our code to public repository like Github, Gitlab, BitBucket, etc, so that you have an URL of your code. You need an URL to the repo.
- Of course you are not forced to use git, but this component is designed with git repository in mind, so it is simpler to do so.

#### The POM model details

- You need some details about your components so that they are submitted to repositories along with your components, These information includes group ID, artifact name, version, you or your organization information, your code repository information, etc.

## Installation

New style with Kotlin script:

```kotlin
plugins {
    id("io.hkhc.simplepublisher") 
}
```

New style with Groovy script

```groovy
plugins {
    id "io.hkhc.simplepublisher"
}
```

Old style with Kotlin script:

```kotlin
buildscript {
    // either one of mavenCentral or jcenter. The plugin is deployed to both repositories.
    mavenCentral()
    // jcenter()
    classpath("io.hkhc.gradle:simplepublisher:0.2")
}

apply(plugin = "io.hkhc.simplepublisher")
```

Old style with Groovy script

```groovy
buildscript {
    // either one of mavenCentral or jcenter. The plugin is deployed to both repositories.
    mavenCentral()
    // jcenter()
    classpath "io.hkhc.gradle:simplepublisher:0.2"
}

apply plugin "io.hkhc.simplepublisher"
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

Given we have setting above in place,  we need group ID and version only for the simplest scenario to publish artifacts to Maven local repository.

```kotlin
group = "io.github.myusername"
version = "1.0-SNAPSHOT"
```

The plugin creates the `publishing` configuration, the corresponding `publication` and a number of tasks with prefix `sp` and under group "SimplePublisher".

| Task                         | Description                                                                     |
| ---------------------------- | ------------------------------------------------------------------------------- |
| `spPublish`                  | Publish Maven publication 'Lib' to Maven Local, 'Mavenlib' Repository, Bintray. |
| `spPublishToBintray`         | Publish Maven publication 'lib' to Bintray                                      |
| `spPublishToMavenLocal`      | Publish Maven publication 'lib' to the local Maven Repository                   |
| `spPublishToMavenRepository` | Publish Maven publication 'lib' to the 'MavenLib' Repository                    |

So we may just issue the command to publish artifacts to Maven local repository. That should include the jar file of library, the jar file of source code archive, and a minimal POM file for the artifacts.

```bash
./gradlew spPublishToMavenLocal
```

One can check the published files under  `~/.m2/repository/io/github/myusername/1.0-SNAPSHOT` directory.

As a side note, for other Gradle project that make use of the artifacts in Maven local repository, it should have the following snippet at the `buildscript` block:

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
WARNING: [simplepublisher] No signing setting is provided. Signing operation is ignored. Maven Central publishing cannot be done without signing the artifacts.
```

It would be technically fine for locally used component without documentation archive and signature of artifacts. But they are mandatory for Maven Central publishing.

## Configuration in build script

We may add gradle extension `simplyPublish`to configure what to do with the plugin. 

```kotlin
simplyPublish {
    // our customization here.
}
```

## Signing configuration

We setup the singing details in gradle.properties as specify in Gradle [doc]([The Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)). Gradle signing plugin provide a few ways to configure how artifacts are signed.

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
  
  We could add the `useGpg` flag to determine the which way we configure the keys.
  
  ```kotlin
  simplyPublish {
      useGpg = true // true for v2, and false for v1
  }
  ```
  
  The plugin try to examine the setting in `gradle.properties` if one of the configuration types is available and it does not match what we set in `simplyPlugin` block, the plugin override it and show a warning like this:
  
  ```
  WARNING: [simplepublisher] Setting to use gpg keyring file but signing gpg keybox  configuration is found. Switch to use gpg keybox
  ```
  
  if we have `--info` option in Gradle command line.
  
  If signing information is incomplete, the signing operation is ignored and we get the following warning
  
  ```
  WARNING: [simplepublisher] No signing setting is provided. Signing operation is ignored. Maven Central publishing cannot be done without signing the artifacts.
  ```

## POM Declaration

### Simplest case

In simplest form to deploy artifacts to local Maven repository, we don't need any settings beyond `group` and `version` in build script. For example,

```
group = "mygroup"
version = "1.0"
```

The artifact ID will be project name if we don't specify one. The plugin generate the necessary `publishing` block and `publication` block that we used to create them manually. 

So we can just start building our artifacts with this.

```
./gradlew spPublishToMavenLocal
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

To publish it to public repository like Maven Central, we need to provide more information in the form of [POM XML](https://maven.apache.org/pom.html). Traditionally we provide these POM information in hand coded publishing section. However to make things more complicated, when we publish our artifacts to multiple repository, like Maven Central, jCenter, and Gradle plugin portal, we need to specify the same piece of information multiple times at different plugin configurations. The pubished POM XML is generated by these plugins. This create significant amount of boilerplate code in build script. Simple publisher is aimed at simplify this process by having a single place to specify POM information.

Simple Publisher read POM information from a file called pom.yml. It is YAML format means to be human friendly to specify POM details.

The plugin read pom.yml at several places. The plugin read pom.yml at the following order:

- `$GRADLE_USER_HOME/pom.yml`

- `[project-root-dir]/pom.yml`

- `[sub-project-dir]/pom.yml`

- file specified by `pomFile` system property. 

The rule of thumb is that the files will be sitting side-by-side with gradle.properties. By putting some of the information out of the project directory, some common information can be shared among projects, like developer information.

The content of later files overwrite content of the preceding files. The content of the files will be used to generate the configuration of maven publication, bintray publication, and gradle plugin portal.

### Format of pom.yml

Most of the content within pom.yml could be optional. However, it take a full pom.yml file to satisfy Maven Central requirement.

This is the pom.yml from the Simple Publisher source code. Yes, it make use of itself to build and publish artifact.

```yaml
# group name of the artifact, use project.group if omitted
group: io.hkhc.gradle
artifactId: simplepublisher
name: Simple Publisher
version: 0.3.3.2
description: Wrapping boilerplate code of publishing artifact to maven repository
packaging: jar

licenses:
- name: Apache-2.0

developers:
- id: hkhc
  name: Herman Cheung
  email: herman.kh.cheung+simplepublisher@gmail.com

scm:
  repoType: github.com
  repoName: hkhc/simplepublisher

plugin:
  id: io.hkhc.simplepublisher
  displayName: Simple Publisher
  implementationClass: io.hkhc.gradle.SimplePublisherPlugin
  tags:
  - publish
```

### Coordinate

The combination `group:artifactId:version` is known as the "coordinate" of the artifact. if pom.yml is not provided, the `project.group`, `project.name` and `project.version` is used to form the coordinate.

Otherwise, the `group`, `artifactId`, `version` in pom.yml will be used.

If both values in build script and pom.yml exist, the value at pom.xml has precedence. 

When publishing to Maven Central, make sure the group name has been properly [verified](https://central.sonatype.org/pages/producers.html#individual-projects-open-source-software-repository-hosting-ossrh).

### Artifact name

`name`  field is a descriptive name of the artifact. If it is not specified, `group:artifactId` will be used as name.

### License

We can specify multiple licenses for the artifact. Most of the time we need only one. In its simplest form, we may just specify the `name` field under `licenses`. For example

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

- License information of different name are treated as separate licenses in merged information.

- License information at pom.xml of higher precedence overwrite the license of the same name in file of lower precedence, in field by field basis.

### Developers and Contributors

We can specify multiple developers and contributors in pom.yml. Both of them share the same structure. Minimally, one developer entry should be provided which contains `name`, `email`.  In full, it accept the following fields:

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

If they are omitted, the url field is replaced by the value of Scm.url, and the description field is replaced by the description of the artifact.

### Url

The project has its url field. If it is omitted, the Scm.url field is used. 

## Minimal requirement of POM for Maven Central

While the information in pom.yml are mostly optional, we need them all such that Maven Central repository accept our submission.
