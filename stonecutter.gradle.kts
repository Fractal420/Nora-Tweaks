plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2"

stonecutter.tasks {
    order("build")
}

tasks.register("buildAll") {
    group = "build"
    description = "Build the mod for every supported Minecraft version"
    dependsOn(":1.21.11:build", ":26.1.2:build", ":26.2:build")
}
