package mmap.features.testing.di

import mmap.core.IgnoreCoverage
import mmap.features.testing.TestingController
import org.koin.dsl.module

@IgnoreCoverage
val featureTestsModule = module {
    factory {
        TestingController(
            testingRepository = get()
        )
    }
}
