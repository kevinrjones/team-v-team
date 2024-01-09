import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application

    alias(libs.plugins.versionUpdate)
    alias(libs.plugins.catalogUpdate)
//    alias(libs.plugins.jooq)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.jooqCodeGen)
        classpath(libs.mariadb)
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

    implementation(libs.mariadb)

    implementation(libs.jooq)
    implementation(libs.jooqCodeGen)
    implementation(libs.jooqMeta)
    implementation(libs.kotlinCoroutines)
    implementation(libs.kotlinxSerialization)
    implementation(libs.kotlinxDatetime)


    implementation(libs.kotlinxHtmlJvm)
    implementation(libs.kotlinxHtml)

    implementation(libs.commonsCli)

//    jooqCodegen(libs.mariadb)
}

application {
    mainClass.set("com.knowledgespike.teamvteam.Application")

    applicationDefaultJvmArgs = listOf("-Dlogback.configurationFile=./logging/logback.xml")
}

sourceSets {
    this.main {
        val generatedDir: Provider<Directory> = layout.buildDirectory.dir("generated/java")
        java.srcDir(generatedDir)
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

//jooq {
//    configuration {
//        val output: Provider<Directory> = layout.buildDirectory.dir(".")
//        basedir = "%{output.get()}"
//        jdbc {
//            driver = "org.mariadb.jdbc.Driver"
//            url = "jdbc:mariadb://localhost:3306/cricketarchive"
//            user = "cricketarchive"
//            password = "p4ssw0rd"
//        }
//        generator {
//            name = "org.jooq.codegen.KotlinGenerator"
//            database {
//                inputSchema = "cricketarchive"
//                name = "org.jooq.meta.mariadb.MariaDBDatabase"
//            }
//            generator {
//                target {
//                    packageName = "com.knowledgespike.db"
//                    directory = "generated/java"
//                    isClean = true
//                }
//            }
//        }
//    }
//}
//
//tasks.compileKotlin {
//    dependsOn(tasks["jooqCodegen"])
//}

tasks.create("generateJOOQ") {
    val output: Provider<Directory> = layout.buildDirectory.dir(".")

    GenerationTool.generate(
        Configuration()
            .withBasedir("${output.get()}")
            .withJdbc(
                Jdbc()
                    .withDriver("org.mariadb.jdbc.Driver")
                    .withUrl("jdbc:mariadb://localhost:3306/cricketarchive")
                    .withUser("cricketarchive")
                    .withPassword("p4ssw0rd")
            )

            .withGenerator(
                Generator()
                    .withName("org.jooq.codegen.KotlinGenerator")
                    .withDatabase(
                        Database()
                            .withInputSchema("cricketarchive")
                            .withName("org.jooq.meta.mariadb.MariaDBDatabase")
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