package io.cloudflight.gradle.autoconfigure.springdocopenapi

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class SpringDocOpenApiConfigurePluginTest {

    @Test
    fun `the openapi document is created in a single module project`():
            Unit = springdocFixture("simple") {
        val result = run("clfGenerateOpenApiDocumentation")

        assertThat(buildDir().resolve("generated/resources/openapi/springdoc-openapi.json")).exists()
        assertThat(buildDir().resolve("generated/resources/openapi/springdoc-openapi.yaml")).exists()
    }


    @Test
    fun `the openapi document is created in a multi module project`():
            Unit = springdocFixture("kotlin-springboot-angular") {
        val result = run("clfGenerateOpenApiDocumentation")

        assertThat(buildDir("skeleton-server").resolve("generated/resources/openapi/springdoc-openapi.json")).exists()
        assertThat(buildDir("skeleton-server").resolve("generated/resources/openapi/springdoc-openapi.yaml")).exists()
    }
}

private fun <T : Any> springdocFixture(fixtureName: String, testWork: ProjectFixture.() -> T): T =
    useFixture("springdocopenapi", fixtureName, null, emptyMap(), testWork)