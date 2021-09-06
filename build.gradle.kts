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
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("kabinet") {
            from(components["java"])

            infix fun <T> Property<T>.by(value: T) {
                set(value)
            }

            pom {
                name by project.name
                description by "A barebone data mapping library in Kotlin"
                url by "https://github.com/testinfected/Kabinet"

                licenses {
                    license {
                        name by "BSD 3-Clause License"
                        url by "https://opensource.org/licenses/BSD-3-Clause"
                        distribution by "repo"
                    }
                }

                developers {
                    developer {
                        id by "testinfected"
                        name by "Vincent Tencé"
                        organization by "Bee Software"
                        organizationUrl by "http://bee.software"
                    }
                }

                scm {
                    url by "https://github.com/testinfected/Kabinet"
                    connection by "https://github.com/testinfected/Kabinet.git"
                    developerConnection by "git@github.com:testinfected/Kabinet.git"
                }

            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
        }
    }
}

signing {
    sign(publishing.publications["kabinet"])
}