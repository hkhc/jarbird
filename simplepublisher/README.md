# SimplePublisher Gradle Plugin

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
	id "io.hkhc.simplepublisher")
}
```

Old style with Kotlin script:

```
buildscript {
	// either one of mavenCentral or jcenter. The plugin is deployed to both repositories.
	mavenCentral()
	jcenter()
	classpath "io.hkhc.gradle:simplepublisher:0.2"
}
```

