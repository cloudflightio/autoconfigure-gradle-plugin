description = "Cloudflight Angular Kotlin Skeleton"
group = "io.cloudflight.skeleton.angular"

fun isJavaApplied(target: Project): Boolean {
    return target.plugins.hasPlugin("java")
}

fun isKotlinApplied(target: Project): Boolean {
    return target.plugins.hasPlugin("kotlin-jvm")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
        if (isJavaApplied(this)) {
            dependencies {
                "implementation"(platform(libs.cloudflight.platform.spring.bom))
                "annotationProcessor"(platform(libs.cloudflight.platform.spring.bom))
                "testImplementation"(platform(libs.cloudflight.platform.spring.test.bom))
            }
        }

        if (isKotlinApplied(this)) {
            dependencies {
                "kapt"(platform(libs.cloudflight.platform.spring.bom))
            }
        }
    }
}
