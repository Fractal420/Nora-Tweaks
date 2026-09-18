plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") version "1.17.20"
    id("maven-publish")
}

fun prop(name: String): String =
    project.findProperty(name)?.toString()
        ?: rootProject.findProperty(name)?.toString()
        ?: error("Missing property: $name")

val minecraftVersion = prop("minecraft_version")
val loaderVersion = prop("loader_version")
val fabricApiVersion = prop("fabric_api_version")
val meteorVersion = prop("meteor_version")
val baritoneVersion = prop("baritone_version")
val modVersion = prop("mod_version")
val mavenGroup = prop("maven_group")
val archivesBaseName = prop("archives_base_name")

version = "$modVersion-$minecraftVersion"
group = mavenGroup

base {
    archivesName.set(archivesBaseName)
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://libraries.minecraft.net/")
    maven("https://maven.meteordev.org/releases")
    maven("https://maven.meteordev.org/snapshots")
    maven("https://maven.xpple.dev/maven2")
    maven("https://jitpack.io")
    flatDir { dirs(rootProject.file("libs")) }
    mavenCentral()
    mavenLocal()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modCompileOnly("meteordevelopment:meteor-client:$meteorVersion-SNAPSHOT")
    compileOnly("meteordevelopment:orbit:0.2.4")
    compileOnly("meteordevelopment:baritone:$baritoneVersion-SNAPSHOT") {
        isTransitive = false
    }
    implementation("mixinsquared-fabric:mixinsquared-fabric:0.2.0")
    include("mixinsquared-fabric:mixinsquared-fabric:0.2.0")
}

tasks {
    processResources {
        val ver = project.version.toString()
        val mc = minecraftVersion
        inputs.property("version", ver)
        inputs.property("minecraft_version", mc)
        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") {
            filter { line: String ->
                line
                    .replace("\${version}", ver)
                    .replace("\${minecraft_version}", mc)
            }
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 21
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        withSourcesJar()
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$archivesBaseName" }
        }
    }
}
