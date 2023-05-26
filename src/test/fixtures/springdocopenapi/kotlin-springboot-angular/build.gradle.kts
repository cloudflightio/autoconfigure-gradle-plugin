import io.cloudflight.gradle.util.BuildUtils.isJavaApplied
import io.cloudflight.gradle.util.BuildUtils.isKotlinApplied

description = "Cloudflight Angular Kotlin Skeleton"
group = "io.cloudflight.skeleton.angular"

cloudflight {
    softwareProjectKey.set("cloudflight/skeletons")
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
