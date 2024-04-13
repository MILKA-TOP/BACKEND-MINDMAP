package mmap.features.catalog.di

import mmap.features.catalog.CatalogController
import org.koin.dsl.module

val featureCatalogModule = module {
    factory {
        CatalogController(
            catalogRepository = get()
        )
    }
}
