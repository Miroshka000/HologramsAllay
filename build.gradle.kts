plugins {
    id("java-library")
    id("org.allaymc.gradle.plugin").version("0.2.1")
}

group = "miroshka.holograms"
version = "1.0.0"
description = "Holograms For Allay"

allay {
    api = "0.18.0"

    apiOnly = true

    server = null

    generatePluginDescriptor = true

    plugin {
        entrance = "miroshka.holograms.Holograms"
        apiVersion = ">=0.18.0"
        authors += "Miroshka"
        dependency("PlaceholderAPI")
    }
}

dependencies {
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.34")
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
    compileOnly(group = "org.allaymc", name = "papi", version = "0.1.1")
}