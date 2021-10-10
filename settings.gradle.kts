rootProject.name = "akka-spring-kotlin"
include("akka-kotlin")
include("akka-spring")

pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
    }
}