plugins {
    id ("io.cloudflight.autoconfigure.swagger-api-configure")
}

dependencies {
    implementation("io.swagger:swagger-annotations")
    implementation(libs.swagger.annotations)

    implementation("org.springframework:spring-web")
}
