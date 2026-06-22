plugins {
    kotlin("plugin.jpa") version "2.2.21"
}
dependencies {
    implementation(project(":modules:bank-account"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("com.h2database:h2")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("tools.jackson.module:jackson-module-kotlin")
}
repositories {
    mavenCentral()
}
