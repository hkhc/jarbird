# SimplePublisher Gradle Plugin

## Introduction

Publishing libraries to public repositories like Maven, Bintray are not really difficult at all, but it is a nausance, especially for first-timer. Setting up accounts are lengthy process, and we need to set our build script right so that those repositories accept our submission. By "right" we means using proper plugin, declaring proper publication, and executing proper tasks. Once we have set up all those things, it is mostly "just work". So there is a signifacant "fiction" to make our components available in repositories rather than copy-and-paste among projects.

Worst still, the code to publish artifacts to maven is quite different from that for bintray repository. We need double effort if we want to publish to both repository. 

The result gradle script is filled with boilerplate code. It is hard to read and the project specific details is buried in the junkle of tasks and configurations.

"SimplePublisher" Gradle plugin is designed to make these things simple. Simply speaking, this plugin wrap up the boilerplate code to publish components to major public repositories, including Maven Central, Bintray/JCenter, Gradle's own plugin repository. Its aim is making simple thing simple, so undoubly there are things that can be done by hand-code the publishing code in gradle build script, but cannot be done by this plugin. However, if thing is really complicate, it deserve effort to do it in good old way.

## You need the following before you start

You need quite amount of preparation to publish your first component. Most of them are one off that you may use them for publishing unlimited number of components. A few others are per component settings that you need to have it for each publication.

### One time setup:

#### Bintray

- Create bintray account (if you want to publish to jCenter), so that you have username and password of the account.

#### Maven Central

- Sanotype account (if you want to publish to Maven Central), so that you have username and password of the account
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
	jcenter()
	classpath("io.hkhc.gradle:simplepublisher:0.2")
}

apply(plugin = "io.hkhc.simplepublisher")
```

Old style with Groovy script

```groovy
buildscript {
	// either one of mavenCentral or jcenter. The plugin is deployed to both repositories.
	mavenCentral()
	jcenter()
	classpath "io.hkhc.gradle:simplepublisher:0.2"
}

apply plugin "io.hkhc.simplepublisher"
```

## Configuration

For the simplist scenario, we don't need additional setting at all. The plugin generate the boliteplate configuration:
- You 




