plugins {
    kotlin("jvm") version "2.1.20"
    id("com.google.devtools.ksp") version "2.2.21-2.0.4"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "org.core.embedding"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

}
dependencies {
    implementation("io.insert-koin:koin-core:3.5.3")
    implementation("io.insert-koin:koin-core-coroutines:3.5.3")

    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation(project(":core:common"))
}
kotlin {
    jvmToolchain(21)
}
