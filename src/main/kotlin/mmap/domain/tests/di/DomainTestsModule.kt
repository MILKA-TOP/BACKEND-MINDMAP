package mmap.domain.tests.di

import mmap.core.IgnoreCoverage
import mmap.domain.tests.TestsRepository
import org.koin.dsl.module

@IgnoreCoverage
val domainTestsModule = module {
    factory {
        TestsRepository(
            mapsDataSource = get(),
            yandex300DataSource = get(),
            opexamsDataSource = get(),
        )
    }
}
