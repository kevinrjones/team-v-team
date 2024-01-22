plugins {
    alias(libs.plugins.jooq)
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

//sourceSets {
//    this.main {
//        val generatedDir: Provider<Directory> = layout.buildDirectory.dir("generated/java")
//        java.srcDir(generatedDir)
//    }
//}



jooq {
//    val output: Provider<Directory> = layout.buildDirectory.dir(".")
    configuration {
//        basedir = "${output.get()}"
        jdbc {
            driver = "org.mariadb.jdbc.Driver"
            url = "jdbc:mariadb://localhost:3306/cricketarchive"
            user = "cricketarchive"
            password = "p4ssw0rd"
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                inputSchema = "cricketarchive"
                name = "org.jooq.meta.mariadb.MariaDBDatabase"
            }

            target {
                packageName = "com.knowledgespike.db"
                directory = "generated/java"
                isClean = true
            }
        }
    }
}

tasks.compileKotlin {
    dependsOn(tasks["jooqCodegen"])
}
