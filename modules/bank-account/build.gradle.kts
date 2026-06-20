plugins {
    kotlin("plugin.jpa")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("com.h2database:h2")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}
