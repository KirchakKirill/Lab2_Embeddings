plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Embedding1"
include(":core:common")
include(":core:db")
include(":core:network")
include(":core:llm")
include(":llm:embedding")
