plugins {
    `java-library`
    `maven-publish`
    application
    kotlin("plugin.lombok") version "1.8.21"
    id("io.freefair.lombok") version "5.3.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("application")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://m2.dv8tion.net/releases")
    }

    maven {
        url = uri("https://repo.opencollab.dev/maven-releases/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("com.github.tycrek:MCAuthLib:3.0")
    implementation("com.github.steveice10:mcprotocollib:1.19.2-1")
    implementation("net.dv8tion:JDA:5.0.0-alpha.9")
    implementation("org.json:json:LATEST")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("club.minnced:discord-webhooks:0.8.2")
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

group = "org.example"
version = "1.0-SNAPSHOT"
description = "Dax"

java.sourceCompatibility = JavaVersion.VERSION_20

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    shadowJar {
        archiveFileName.set("Dax.jar")
    }
}

application {
    mainClass.set("me.loudbook.discordlink.Main")
}
