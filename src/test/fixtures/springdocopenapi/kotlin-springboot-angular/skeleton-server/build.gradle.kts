plugins {
   id("io.cloudflight.autoconfigure.springdoc-openapi-configure")
}

dependencies {
    implementation(project(":skeleton-api"))
    implementation(project(":skeleton-ui"))

    implementation("io.cloudflight.platform.spring:platform-spring-i18n")
    implementation("io.cloudflight.platform.spring:platform-spring-logging")
    implementation("io.cloudflight.platform.spring:platform-spring-json")
    implementation("io.cloudflight.platform.spring:platform-spring-server-config")
    testImplementation("io.cloudflight.platform.spring:platform-spring-test")
    testImplementation("io.cloudflight.platform.spring:platform-spring-test-archunit")

    implementation("org.springframework.boot:spring-boot-actuator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(libs.springdoc.openapi.starter.webmvc.api)
}

tasks.named("forkedSpringBootRun") {
    doNotTrackState("We cannot track the state during the test since the working directory is already blocked by the test-executing gradle-process.")
}

openApi {
    outputFileName.set("custom-openapi.json")
}
