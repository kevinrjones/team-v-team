import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {

    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.versionUpdate)
    alias(libs.plugins.catalogUpdate)
}

allprojects {
    group = "com.knowledgespike"
    version = "0.1.0"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "kotlinx-serialization")

    repositories {
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
    }

    dependencies {

        testImplementation(kotlin("test"))
        implementation(kotlin("stdlib-jdk8"))

        implementation(rootProject.libs.logback)

        testImplementation(rootProject.libs.junit)
        testImplementation(rootProject.libs.jUnitEngine)

    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // config JVM target to 1.8 for kotlin compilation tasks
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "21"
        kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }

}

subprojects {
    version = "1.0"

}

project(":app_shared") {
    dependencies {

        implementation(rootProject.libs.kotlinReflect)

        implementation(rootProject.libs.jooq)
        implementation(rootProject.libs.jooqCodeGen)
        implementation(rootProject.libs.jooqMeta)

        implementation(rootProject.libs.kotlinxSerialization)
        implementation(rootProject.libs.kotlinxDatetime)
        implementation(rootProject.libs.mariadb)

        implementation(rootProject.libs.kotlinxHtmlJvm)
        implementation(rootProject.libs.kotlinxHtml)

    }
}

project(":app_tvt") {

    dependencies {
        implementation(project(":app_shared"))

        implementation(kotlin("stdlib-jdk8"))

        implementation(rootProject.libs.logback)

        implementation(rootProject.libs.mariadb)

        implementation(rootProject.libs.jooq)
        implementation(rootProject.libs.jooqCodeGen)
        implementation(rootProject.libs.jooqMeta)
        implementation(rootProject.libs.kotlinCoroutines)
        implementation(rootProject.libs.kotlinxSerialization)
        implementation(rootProject.libs.kotlinxDatetime)


        implementation(rootProject.libs.kotlinxHtmlJvm)
        implementation(rootProject.libs.kotlinxHtml)

        implementation(rootProject.libs.commonsCli)

        testImplementation(kotlin("test"))
    }
}

project(":app_progressive") {
    dependencies {
        implementation(project(":app_shared"))
        implementation(kotlin("stdlib-jdk8"))

        implementation(rootProject.libs.logback)

        implementation(rootProject.libs.mariadb)

        implementation(rootProject.libs.jooq)
        implementation(rootProject.libs.jooqCodeGen)
        implementation(rootProject.libs.jooqMeta)
        implementation(rootProject.libs.kotlinCoroutines)
        implementation(rootProject.libs.kotlinxSerialization)
        implementation(rootProject.libs.kotlinxDatetime)


        implementation(rootProject.libs.kotlinxHtmlJvm)
        implementation(rootProject.libs.kotlinxHtml)

        implementation(rootProject.libs.commonsCli)

        testImplementation(kotlin("test"))
    }
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