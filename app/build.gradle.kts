import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target

val logbackVersion: String by project
val jooqVersion: String by project
val kotlinVersion: String by project
val kotlinxHtmlVersion: String by project
val mySqlVersion: String by project
val apacheCommonsCli: String by project
val coroutinesVersion: String by project
val kotlinSerializationVersion: String by project

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.10"
    application
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jooq:jooq-codegen:3.18.5")
        classpath("mysql:mysql-connector-java:8.0.33")
    }
}


group = "com.knowledgespike"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")

    implementation("mysql:mysql-connector-java:$mySqlVersion")

    implementation("org.jooq:jooq:$jooqVersion")
    implementation("org.jooq:jooq-codegen:$jooqVersion")
    implementation("org.jooq:jooq-meta:$jooqVersion")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")


    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")

    implementation("commons-cli:commons-cli:$apacheCommonsCli")
}

application {
    mainClass.set("com.knowledgespike.teamvteam.Application")

    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=./logging/logback.xml")
}

sourceSets {
    this.main {
        java.srcDir("$buildDir/generated/java")
//        kotlin.srcDir("$projectDir/src/generated/kotlin")
    }
}

//sourceSets.create("generated") {
//
////    kotlin.srcDir("generated/java")
//        java.srcDir("$projectDir/src/generated/java")
//
//}


tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.create("generateJOOQ") {
    GenerationTool.generate(
        Configuration()
            .withBasedir("$buildDir")
//            .withBasedir("$projectDir/src/")
            .withJdbc(
                Jdbc()
                    .withDriver("com.mysql.cj.jdbc.Driver")
                    .withUrl("jdbc:mysql://localhost:3306/cricketarchive")
                    .withUser("cricketarchive")
                    .withPassword("p4ssw0rd")
            )

            .withGenerator(
                Generator()
                    .withName("org.jooq.codegen.KotlinGenerator")
                    .withDatabase(
                        Database()
                            .withInputSchema("cricketarchive")
                            .withName("org.jooq.meta.mysql.MySQLDatabase")
                    )
                    .withGenerate(Generate())
                    .withTarget(
                        Target()
                            .withPackageName("com.knowledgespike.db")
                            .withDirectory("generated/java")
                            .withClean(true)
                    )
            )
    )
}