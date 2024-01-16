plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

group = "com.knowledgespike"
version = "1.0-SNAPSHOT"


application {
    mainClass.set("com.knowledgespike.progressive.Application")

    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=./logging/logback.xml")
}



