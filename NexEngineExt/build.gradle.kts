/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("su.nexmedia.java-conventions")
}

dependencies {
    compileOnly(project(":NexEngineAPI"))

    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    // My own library
    compileOnlyApi("cc.mewcraft:MewCore:5.13.1")

    // 3rd party plugins that may contain random transitive dependencies
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { isTransitive = false }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.6") {
        exclude("org.bukkit")
    }
    compileOnly("net.citizensnpcs:citizensapi:2.0.29-SNAPSHOT") {
        exclude("ch.ethz.globis.phtree")
    }
    compileOnly("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.2.1") { isTransitive = false }
}

description = "NexEngineExt"

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