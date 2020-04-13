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
- Apply for your own group name. The simplest way is to use io.github.username as your group ID. Otherwise, You may need to have full control of an internet domain name, and have right to update the TXT record of your domain  to get though the procedure. One group name can be used by multiple components, and of course nothing stop you from create one group ID for each of your project.

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
  - io.github.hkhc:helloworld:1.0-SNAPSHOT 
  - maven local
- Add dokka
- Add signing info
- pom yml
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

The plugin creates the `publishing` configuration, the corresponding `publication` and a number of tasks with prefix `sp` and under group "simplepublisher". 

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



## Signing configuration

We setup the singing details in gradle.properties as specify in Gradle [doc]([The Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)). Gradle 



### Minimal requirements of POM

- groupId

- artifactId

- version

- name

- description

- url

- license

- developer

- scm: 
  
  - connection
  
  - developerconnection
  
  - url
  
  - 
