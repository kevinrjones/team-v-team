rootProject.name = "Produce Stats"

include("app_tvt")
include("app_progressive")
include("app_shared")

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.gradle.toolchains:foojay-resolver:0.7.0")
    }
}

apply(plugin = "org.gradle.toolchains.foojay-resolver-convention")

//toolchainManagement {
//    jvm {
//        javaRepositories {
//            repository("foojay") {
//                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
//            }
//        }
//    }
//}

