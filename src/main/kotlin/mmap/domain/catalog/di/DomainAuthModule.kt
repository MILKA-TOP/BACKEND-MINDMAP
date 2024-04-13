package mmap.domain.catalog.di

import mmap.domain.catalog.CatalogRepository
import org.koin.dsl.module

val domainCatalogModule = module {
    factory {
        CatalogRepository(
            mapsDataSource = get(),
        )
    }
}
