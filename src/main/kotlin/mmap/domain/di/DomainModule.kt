package mmap.domain.di

import mmap.core.IgnoreCoverage
import mmap.domain.auth.di.domainAuthModule
import mmap.domain.catalog.di.domainCatalogModule
import mmap.domain.maps.di.domainMapsModule
import mmap.domain.nodes.di.domainNodesModule
import mmap.domain.tests.di.domainTestsModule
import org.koin.dsl.module

@IgnoreCoverage
val domainModule = module {
    includes(
        domainAuthModule,
        domainCatalogModule,
        domainNodesModule,
        domainTestsModule,
        domainMapsModule,
    )
}
