package io.cloudflight.gradle.autoconfigure.skeleton

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream
import java.util.zip.ZipFile

data class TestOptions(
    val fixtureName: String,
    val swaggerFileName: String = fixtureName
)

class SkeletonPluginTest {

    @ParameterizedTest
    @MethodSource("skeletonArguments")
    fun `the skeleton application is build correctly`(
        options: TestOptions
    ): Unit = swaggerFixture(options.fixtureName) {
        run(LifecycleBasePlugin.CLEAN_TASK_NAME, LifecycleBasePlugin.BUILD_TASK_NAME)

        val uiJar = this.fixtureDir.resolve("skeleton-ui/build/libs/skeleton-ui-1.0.0.jar")
        assertThat(uiJar).exists()

        ZipFile(uiJar.toFile()).use { zipFile ->
            assertThat(zipFile.getEntry("META-INF/MANIFEST.MF")).isNotNull
            assertThat(zipFile.getEntry("static/index.html")).isNotNull
        }
    }

    companion object {

        @JvmStatic
        fun skeletonArguments(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "kotlin-springboot-angular",
                    )
                )
            )
        }
    }
}

private val SKELETON_FIXTURE_PATH = Paths.get("skeletons")
private fun <T : Any> swaggerFixture(fixtureName: String, testWork: ProjectFixture.() -> T): T =
    useFixture(SKELETON_FIXTURE_PATH, fixtureName, null, emptyMap(), testWork)