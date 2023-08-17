plugins {
    id("io.cloudflight.autoconfigure.node-configure")
    id("io.cloudflight.autoconfigure.swagger-codegen-configure")
}

dependencies {
    swaggerApi(project(":skeleton-api"))
    swaggerUI(libs.swagger.ui)
}

project.gradle.projectsEvaluated {
    tasks.getByName("generateSwaggerUISkeleton-api").dependsOn(":skeleton-api:jar")
}

tasks.processResources {
    from(file("build/swagger-ui-skeleton-api")) {
        into("static/swagger-ui")
    }
    dependsOn("generateSwaggerUI")
}
