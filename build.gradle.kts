plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "1.5.0"
    id("org.liquibase.gradle") version "2.2.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.liquibase:liquibase-core:4.25.1")
    liquibaseRuntime("org.liquibase:liquibase-core:4.25.1")
    liquibaseRuntime("org.postgresql:postgresql:42.7.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("com.pgvector:pgvector:0.1.5")
    implementation("org.postgresql:postgresql:42.7.2")
    testImplementation(kotlin("test"))

    // REQUIRED for Liquibase 4.x
    liquibaseRuntime("info.picocli:picocli:4.7.5")
    liquibaseRuntime("org.yaml:snakeyaml:2.2")

    // Add ALL these runtime dependencies
    liquibaseRuntime("javax.xml.bind:jaxb-api:2.3.1")
    liquibaseRuntime("com.sun.xml.bind:jaxb-core:2.3.0.1")
    liquibaseRuntime("com.sun.xml.bind:jaxb-impl:2.3.5")
    liquibaseRuntime("javax.activation:activation:1.1.1")

    // Logging
    liquibaseRuntime("ch.qos.logback:logback-classic:1.4.14")
    liquibaseRuntime("org.slf4j:slf4j-api:2.0.9")

    // If using XML changelogs
    liquibaseRuntime("org.liquibase.ext:liquibase-percona:4.27.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

liquibase {
    activities {
        register("main") {
            this.arguments = mapOf(
                "changelogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
                "url" to "jdbc:postgresql://localhost:5433/PGVector",
                "username" to System.getenv("DB_USERNAME"),
                "password" to System.getenv("DB_PASSWORD"),
                "driver" to "org.postgresql.Driver"
            )
        }
    }
    runList = "main"
}