package mmap.features.catalog.di

import mmap.features.catalog.CatalogController
import mmap.features.nodes.NodesModifyController
import org.koin.dsl.module

val featureCatalogModule = module {
    factory {
        CatalogController(
            catalogRepository = get()
        )
    }
}
