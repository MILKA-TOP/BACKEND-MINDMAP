package mmap.features.di

import mmap.features.catalog.di.featureCatalogModule
import mmap.features.maps.di.featureMapsModule
import mmap.features.nodes.di.featureNodesModule
import mmap.features.testing.di.featureTestsModule
import mmap.features.user.di.featureUserModule
import org.koin.dsl.module

val featureModule = module {
    includes(
        featureUserModule,
        featureNodesModule,
        featureCatalogModule,
        featureTestsModule,
        featureMapsModule,
    )
}
