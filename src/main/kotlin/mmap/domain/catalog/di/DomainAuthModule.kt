package mmap.domain.catalog.di

import mmap.domain.catalog.CatalogRepository
import mmap.domain.nodes.NodesRepository
import org.koin.dsl.module

val domainCatalogModule = module {
    factory {
        CatalogRepository(
            mapsDataSource = get(),
        )
    }
}
