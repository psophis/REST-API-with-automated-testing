plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("plugin.jpa") version "2.2.21" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0" apply false
}

group = "com.bachelorarbeit"
version = "0.0.1-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    if (buildFile.exists()) {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "jacoco")

        configure<JacocoPluginExtension> {
            toolVersion = "0.8.14"
        }

        tasks.withType<Test> {
            useJUnitPlatform()
            finalizedBy(tasks.named("jacocoTestReport"))
        }

        tasks.named<JacocoReport>("jacocoTestReport") {
            dependsOn(tasks.withType<Test>())

            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }

        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(21)
            }
        }

        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            compilerOptions {
                freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
            }
        }

        dependencies {
            add("implementation", platform("org.springframework.boot:spring-boot-dependencies:4.0.6"))
            add("testImplementation", platform("org.springframework.boot:spring-boot-dependencies:4.0.6"))

            add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
            add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5")
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
repositories {
    mavenCentral()
}
