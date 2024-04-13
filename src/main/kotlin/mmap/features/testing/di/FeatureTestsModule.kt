package mmap.features.testing.di

import mmap.features.testing.TestingController
import org.koin.dsl.module

val featureTestsModule = module {
    factory {
        TestingController(
            testingRepository = get()
        )
    }
}
