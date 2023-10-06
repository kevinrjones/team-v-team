import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

val logbackVersion: String by project
val jooqVersion: String by project
val kotlinVersion: String by project
val kotlinxHtmlVersion: String by project
val mySqlVersion: String by project
val apacheCommonsCli: String by project
val coroutinesVersion: String by project
val kotlinSerializationVersion: String by project

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application

    alias(libs.plugins.versionUpdate)
    alias(libs.plugins.catalogUpdate)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.jooqCodeGen)
        classpath(libs.mysql)
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

    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.kotlinReflect)

    implementation(libs.logback)

    implementation(libs.mysql)

    implementation(libs.jooq)
    implementation(libs.jooqCodeGen)
    implementation(libs.jooqMeta)
    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinxSerialization)


    implementation(libs.kotlinxHtmlJvm)
    implementation(libs.kotlinxHtml)

    implementation(libs.commonsCli)
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

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}