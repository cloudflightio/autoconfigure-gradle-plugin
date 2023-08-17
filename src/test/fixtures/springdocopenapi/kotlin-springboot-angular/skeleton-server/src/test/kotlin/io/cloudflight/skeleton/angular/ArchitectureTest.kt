package io.cloudflight.skeleton.angular

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests
import com.tngtech.archunit.junit.AnalyzeClasses
import io.cloudflight.platform.spring.test.archunit.AbstractCleanCodeTest

@AnalyzeClasses(packagesOf = [ArchitectureTest::class], importOptions = [DoNotIncludeTests::class])
class ArchitectureTest : AbstractCleanCodeTest()
