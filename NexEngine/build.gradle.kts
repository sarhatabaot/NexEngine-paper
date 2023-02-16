/*
 * This file was generated by the Gradle 'init' task.
 */

import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP

plugins {
    id("su.nexmedia.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("net.kyori.indra.git") version "2.1.1"
}

dependencies {
    api(project(":NexEngineAPI"))

    // NMS modules
    api(project(":NMS"))
    runtimeOnly(project(":NexEngineCompat_V1_17_R1", configuration = "reobf"))
    runtimeOnly(project(":NexEngineCompat_V1_18_R2", configuration = "reobf"))
    runtimeOnly(project(":NexEngineCompat_V1_19_R1", configuration = "reobf"))
    runtimeOnly(project(":NexEngineCompat_V1_19_R2", configuration = "reobf"))

    // Internal libraries
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("com.mojang:authlib:3.16.29")
    compileOnly("io.netty:netty-all:4.1.86.Final")
    compileOnly("org.xerial:sqlite-jdbc:3.40.0.0")
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    // 3rd party plugins
    api(project(":NexEngineExt"))
}

description = "NexEngine"
version = "$version".decorateVersion()

bukkit {
    main = "su.nexmedia.engine.NexEngine"
    name = project.name
    version = "${project.version}"
    apiVersion = "1.17"
    authors = listOf("NightExpress")
    softDepend = listOf("Vault", "Citizens", "Brewery", "ItemsAdder", "MMOItems", "MythicLib", "InteractiveBooks")
    load = STARTUP
    libraries = listOf("com.zaxxer:HikariCP:5.0.1", "it.unimi.dsi:fastutil:8.5.11")
}

tasks {
    // Shadow settings
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        minimize {
            exclude(dependency("su.nexmedia:.*:.*"))
        }
        archiveFileName.set("NexEngine-${project.version}.jar")
        archiveClassifier.set("")
        destinationDirectory.set(file("$rootDir"))
    }
    jar {
        archiveClassifier.set("noshade")
    }
    register("deployJar") {
        doLast {
            exec {
                commandLine("rsync", shadowJar.get().archiveFile.get().asFile.absoluteFile, "dev:data/dev/jar")
            }
        }
    }
    register("deployJarFresh") {
        dependsOn(build)
        finalizedBy(named("deployJar"))
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this-${lastCommitHash()}" else this