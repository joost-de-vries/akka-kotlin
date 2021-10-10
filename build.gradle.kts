import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm") apply false
    id("java")
}

allprojects {

    group = "akka-spring-kotlin"
    version = "1.0-SNAPSHOT"
    tasks.withType(KotlinCompile::class).all {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xskip-metadata-version-check",
                "-Xjsr305=strict"
            )
            jvmTarget = "11"
        }
    }
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("java")
    }
}
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}