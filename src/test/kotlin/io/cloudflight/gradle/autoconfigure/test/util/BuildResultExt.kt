package io.cloudflight.gradle.autoconfigure.test.util

import org.gradle.testkit.runner.BuildResult

private val lineEndingRegex = "\r\n|\r".toRegex()

val BuildResult.normalizedOutput: String
    get() {
        return this.output.replace(lineEndingRegex, "\n");
    }