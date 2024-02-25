/*
 * This file was generated by the Gradle 'init' task.
 *
 * This is a general purpose Gradle build.
 * To learn more about Gradle by exploring our Samples at https://docs.gradle.org/8.5/samples
 */
plugins{
    application
    java
    id ("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.springframework.boot") version "3.2.3" 
}
sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
    }
}
apply(plugin = "io.spring.dependency-management")

application {
    mainClass = "com.kuta.Main"
}


repositories{
    mavenCentral()
    gradlePluginPortal()
}

dependencies{
    implementation("com.google.code.gson:gson:2.10.1")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {
  withType<Jar> {
        manifest {
            attributes("Main-Class" to  "com.kuta.Main")
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Exclude duplicates
        configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    }
    shadowJar {
        archiveBaseName.set("alfa_4")
        destinationDirectory.set(File("./"))
        mergeServiceFiles()
        manifest {
            attributes("Main-Class" to "com.kuta.Main")
        }
    }
    bootJar{
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Exclude duplicates
            exclude("META-INF/LICENSE.txt") // Exclude the specific file causing conflict
        archiveFileName.set("app.jar")
        manifest {
            attributes("Main-Class" to "com.kuta.Main")
        }
        archiveBaseName.set("app.jar")
        destinationDirectory.set(file("./"))
    }
}

// Pass default system input to gradle run (Default input stream)
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
