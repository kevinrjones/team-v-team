plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

group = "com.knowledgespike"
version = "1.0-SNAPSHOT"


application {
    mainClass.set("com.knowledgespike.teamvteam.Application")

    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=./logging/logback.xml")
}

kotlin {
    jvmToolchain(17)
}