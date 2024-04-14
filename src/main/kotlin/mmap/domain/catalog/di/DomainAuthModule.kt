package mmap.domain.catalog.di

import mmap.core.IgnoreCoverage
import mmap.domain.catalog.CatalogRepository
import org.koin.dsl.module

@IgnoreCoverage
val domainCatalogModule = module {
    factory {
        CatalogRepository(
            mapsDataSource = get(),
        )
    }
}
