# P2Gradle
this launch an osgi framework with eclipse p2 bundle to perform the task

##Example
gradle kotlin dsl see p2test/build.gradle.kts

####Apply plugin

```kotlin
plugins {
    id ("java-library")
    id ("it.filippor.p2") version ( "0.0.1")
}

repositories {
    mavenCentral()
}

```
the plugin download the eclipse provided bundle from maven central so it requires maven central repository

####Set the updateSite to get the bundle
```kotlin
p2.setUpdateSites( mutableListOf(
	uri("http://download.eclipse.org/releases/2019-12"),
	uri("http://download.eclipse.org/releases/2019-06")
))

```

####define dependencies
```kotlin
dependencies {
  api(p2.bundles("org.eclipse.core.resources:[3.13,3.14)"))
}
```
api is a gradle configuration you can use any configuration.


the version range is defined as

```
	  range ::= interval | atleast
	  interval ::= ( '[' | '(' ) left ',' right ( ']' | ')' )
	  left ::= version
	  right ::= version
	  atleast ::= version
```

the default behavior is to include the transitive dependency to use only declared dependency use

```kotlin
dependencies {
  api(p2.bundles(false,"org.eclipse.core.resources:[3.13,3.14)"))
}
```
####publish to repository
```kotlin
p2.publishTask("p2publish") {
	setRepo(buildDir.toPath().resolve("targetSite").toUri())
	setBundles(configurations.getByName("runtimeClasspath"))
}
```

p2publish is the name of the task that can be invoked

to select the target repository use setRepo with desidered uri

setBundles accept any Iterable<File> and publish the file on repository


###Notes
The local cache for the p2 repository is located at <user.home>/.gradle/caches/p2 but can be configured with 
p2.setAgentUri(URI)


the plugin create a configuration named p2frameworkBundles that contains the bundles installed in the osgi framework to execute the provisioning operation




