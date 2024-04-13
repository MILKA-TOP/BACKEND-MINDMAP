package mmap.domain.tests.di

import mmap.domain.tests.TestsRepository
import org.koin.dsl.module

val domainTestsModule = module {
    factory {
        TestsRepository(
            mapsDataSource = get(),
            yandex300DataSource = get(),
            opexamsDataSource = get(),
        )
    }
}
