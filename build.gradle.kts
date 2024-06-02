plugins {
    `maven-publish`
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.freefair.lombok") version "8.3"
}

group = "com.bof.barn"
version = "1.0.0-SNAPSHOT"
description = "World generator plugin for Barn gamemode"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}


repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    api("com.bof:toolkit:1.0-SNAPSHOT")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.37"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
}

tasks {
    assemble {
        dependsOn(shadowJar)
        dependsOn(reobfJar)
    }

    shadowJar {
        archiveFileName.set("BoFBarnWorldGenerator-${project.version}.jar")
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.20"
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("barn.world-generator") {
            from(components["java"])
        }
    }
}