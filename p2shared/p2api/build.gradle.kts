plugins {
	id("java-library")
}
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("biz.aQute.bnd:biz.aQute.bnd.gradle:4.2.0")
	}
}
apply(plugin = "biz.aQute.bnd.builder")

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("org.osgi:osgi.annotation:7.0.0")
}
