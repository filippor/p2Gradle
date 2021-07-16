plugins {
    id("java-gradle-plugin")
    id("maven-publish")
}

group="it.filippor.p2"
version="0.0.1"

repositories {
	mavenCentral()
    mavenLocal()
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "it.filippor.p2"
            implementationClass = "it.filippor.p2.P2Plugin"
        }
    }
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies{
	implementation("it.filippor.p2:p2api:0.0.1")
	implementation("org.osgi:osgi.core:8.0.0")
	implementation("org.eclipse.platform:org.eclipse.osgi:+")
}