import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.5.20"
}
val coroutinesVersion = "1.5.1"
val akkaVersion = "2.6.9"

group = "akka-kotlin"
version = "1.0-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion:sources")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("com.typesafe.akka:akka-actor_2.13:$akkaVersion")
    implementation("com.typesafe.akka:akka-actor-typed_2.13:$akkaVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("com.typesafe.akka:akka-actor-testkit-typed_2.13:$akkaVersion")
    testImplementation("junit:junit:4.12")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"
