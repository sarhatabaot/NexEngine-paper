/*
 * This file was generated by the Gradle 'init' task.
 */

import io.papermc.paperweight.util.path
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP

plugins {
    id("su.nexmedia.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("net.kyori.indra.git") version "2.1.1"
}

dependencies {
    // NMS modules
    api(project(":NMS"))
    runtimeOnly(project(":NexEngineCompat_V1_17_R1", configuration = "reobf"))
    runtimeOnly(project(":NexEngineCompat_V1_18_R2", configuration = "reobf"))
    runtimeOnly(project(":NexEngineCompat_V1_19_R1", configuration = "reobf"))
    runtimeOnly(project(":NexEngineCompat_V1_19_R2", configuration = "reobf"))

    // Internal libraries
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("com.mojang:authlib:3.16.29")
    compileOnly("io.netty:netty-all:4.1.85.Final")
    compileOnly("org.xerial:sqlite-jdbc:3.40.0.0")
    compileOnly("commons-lang:commons-lang:2.6")
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    // 3rd party plugins
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        isTransitive = false
    }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.6") {
        exclude("org.bukkit")
    }
    compileOnly("net.citizensnpcs:citizensapi:2.0.29-SNAPSHOT")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.0.0")
    compileOnly("io.lumine:Mythic-Dist:5.2.0")
    compileOnly("io.lumine:MythicLib-dist:1.3.4-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.8.3-SNAPSHOT")
    compileOnly("net.leonardo_dgs:InteractiveBooks:1.6.3")
    compileOnly("com.github.DieReicheErethons:Brewery:3.1.1")
    compileOnly("me.clip:placeholderapi:2.10.10")
}

description = "NexEngine"
version = "$version".decorateVersion()

bukkit {
    main = "su.nexmedia.engine.NexEngine"
    name = project.name
    version = "${project.version}"
    apiVersion = "1.17"
    authors = listOf("NightExpress")
    softDepend = listOf("Vault", "Brewery", "ItemsAdder", "MMOItems", "MythicLib", "InteractiveBooks")
    load = STARTUP
    libraries = listOf("com.zaxxer:HikariCP:5.0.1", "it.unimi.dsi:fastutil:8.5.11")
}

tasks {
    val outputFileName = "NexEngine-${project.version}"

    // Shadow settings
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        minimize {
            exclude(dependency("su.nexmedia:.*:.*"))
        }
        archiveFileName.set("$outputFileName.jar")
        archiveClassifier.set("")
        destinationDirectory.set(file("$rootDir"))
    }
    jar {
        archiveClassifier.set("noshade")
    }

//    processResources {
//        val tokens = mapOf(
//            "project.version" to project.version
//        )
//        inputs.properties(tokens)
//    }

    // Copy the output jar to the dev server
    register("deployToServer") {
        dependsOn(build)
        doLast {
            exec {
                commandLine("rsync", "${shadowJar.get().archiveFile.path}", "dev:data/dev/jar")
            }
        }
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