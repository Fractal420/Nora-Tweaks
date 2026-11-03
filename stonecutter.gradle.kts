plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2"

stonecutter.tasks {
    order("build")
}

// Only fully-supported versions in buildAll.
// 1.21.11 is experimental/partial and produces expected Loom remap warnings
// from dependency intermediate names (Baritone, Fabric API). Build it
// explicitly with :1.21.11:build when needed.
tasks.register("buildAll") {
    group = "build"
    description = "Build the mod for every fully-supported Minecraft version (26.1.2 + 26.2)"
    dependsOn(":26.1.2:build", ":26.2:build")
}
