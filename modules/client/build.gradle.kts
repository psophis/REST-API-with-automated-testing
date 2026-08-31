plugins {
    kotlin("plugin.jpa") version "2.2.21"
    id("org.springframework.cloud.contract")
}
dependencies {
    implementation(project(":modules:bank-account"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway")
    testImplementation("org.flywaydb:flyway-database-postgresql")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("tools.jackson.module:jackson-module-kotlin")
    contractTestImplementation(platform("org.springframework.cloud:spring-cloud-contract-dependencies:5.0.3"))
    contractTestImplementation("org.springframework.cloud:spring-cloud-contract-spec-kotlin")
    contractTestImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
    contractTestImplementation("io.mockk:mockk:1.14.6")
    contractTestImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    contractTestImplementation("tools.jackson.module:jackson-module-kotlin")
    contractTestImplementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:2.2.21")
}
repositories {
    mavenCentral()
}

contracts {
    testMode = org.springframework.cloud.contract.verifier.config.TestMode.MOCKMVC
    testFramework = org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5
    baseClassForTests.set("com.bank.client.api.ClientContractBase")
}

sourceSets {
    test {
        resources {
            srcDir(rootProject.file("src/main/resources"))
        }
    }
}
