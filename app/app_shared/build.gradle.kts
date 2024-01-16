import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.jooqCodeGen)
        classpath(libs.mariadb)
    }
}

sourceSets {
    this.main {
        val generatedDir: Provider<Directory> = layout.buildDirectory.dir("generated/java")
        java.srcDir(generatedDir)
    }
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