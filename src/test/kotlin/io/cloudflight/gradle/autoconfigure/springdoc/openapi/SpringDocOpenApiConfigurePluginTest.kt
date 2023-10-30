package io.cloudflight.gradle.autoconfigure.springdoc.openapi

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class SpringDocOpenApiConfigurePluginTest {

    @Test
    fun `the openapi document is created in a single module project with the default configuration`():
            Unit = springdocFixture("simple") {
        run("clean", "clfGenerateOpenApiDocumentation")

        assertThat(buildDir().resolve("generated/resources/openapi/springdoc-openapi.yaml")).exists()
    }

    @Test
    fun `the openapi document is created in a single module project with the json configuration`():
            Unit = springdocFixture("simple-json") {
        run("clean", "clfGenerateOpenApiDocumentation")

        assertThat(buildDir().resolve("generated/resources/openapi/springdoc-openapi.json")).exists()
    }

    @Test
    fun `the openapi documents are created in a single module project with grouped api configuration`():
            Unit = springdocFixture("grouped-api") {
        run("clean", "clfGenerateOpenApiDocumentation")

        assertThat(buildDir().resolve("generated/resources/openapi/groupA.yaml")).exists()
        assertThat(buildDir().resolve("generated/resources/openapi/groupB.yaml")).exists()
    }

    @Test
    fun `the openapi document is created in a multi module project`():
            Unit = springdocFixture("kotlin-springboot-angular") {
        run("clean", "publishToMavenLocal")

        assertThat(buildDir("skeleton-server").resolve("generated/resources/openapi/custom-openapi.json")).exists()
    }
}

private fun <T : Any> springdocFixture(fixtureName: String, testWork: ProjectFixture.() -> T): T =
    useFixture("springdocopenapi", fixtureName, null, emptyMap(), testWork)
