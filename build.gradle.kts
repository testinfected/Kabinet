import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.flywaydb:flyway-core:7.10.0")
    testImplementation("com.h2database:h2:1.4.200")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("failed", "standardOut")
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("kabinet") {
            from(components["java"])
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            stagingProfileId.set("com.vtence")
        }
    }
}

signing {
    sign(publishing.publications["kabinet"])
}