package mmap.features.catalog.di

import mmap.core.IgnoreCoverage
import mmap.features.catalog.CatalogController
import org.koin.dsl.module

@IgnoreCoverage
val featureCatalogModule = module {
    factory {
        CatalogController(
            catalogRepository = get()
        )
    }
}
