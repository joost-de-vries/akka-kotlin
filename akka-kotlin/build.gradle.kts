plugins {
//    id("java")
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    val coroutinesVersion: String by project
    val akkaVersion: String by project
    val scalaVersion: String by project
    val kotlinVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion:sources")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("com.typesafe.akka:akka-actor_$scalaVersion:$akkaVersion")
    implementation("com.typesafe.akka:akka-persistence_$scalaVersion:$akkaVersion")
    testImplementation("com.typesafe.akka:akka-persistence-testkit_$scalaVersion:$akkaVersion")
    implementation("com.typesafe.akka:akka-actor-typed_$scalaVersion:$akkaVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("com.typesafe.akka:akka-actor-testkit-typed_$scalaVersion:$akkaVersion")
    testImplementation("junit:junit:4.12")
}
