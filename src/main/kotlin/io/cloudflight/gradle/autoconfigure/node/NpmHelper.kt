package io.cloudflight.gradle.autoconfigure.node

import io.cloudflight.jsonwrapper.angular.Angular
import io.cloudflight.jsonwrapper.npm.NpmPackage
import org.gradle.api.Project
import java.io.File

object NpmHelper {

    @Suppress("ReturnCount")
    fun determineSourceDirs(project: Project): Set<String> {
        val angularJson = project.file(ANGULAR_JSON)
        if (angularJson.exists()) {
            val angularRoots = Angular.readFromFile(angularJson).getSourceRootsOfAllProjects()
            if (angularRoots.isNotEmpty()) {
                return angularRoots
            } else {
                return setOf("src")
            }
        } else {
            // TODO parse vue or stuff like that
            return setOf("src")
        }
    }

    fun hasScript(script: String, packageJsonFile: File): Boolean {
        val packageJson = readPackageJson(packageJsonFile)
        return packageJson.scripts.containsKey(script)
    }

    fun readPackageJson(file: File): NpmPackage {
        return NpmPackage.readFromFile(file)
    }

    const val ANGULAR_JSON = "angular.json"
    const val PACKAGE_JSON = "package.json"
    const val DOT_NPMRC = ".npmrc"

}