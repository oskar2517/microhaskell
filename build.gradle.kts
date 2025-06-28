import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.oskar.microhaskell"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("microhaskell")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "me.oskar.microhaskell.Main"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}