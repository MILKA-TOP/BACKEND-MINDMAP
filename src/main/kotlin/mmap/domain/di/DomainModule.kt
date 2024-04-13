package mmap.domain.di

import mmap.domain.auth.di.domainAuthModule
import mmap.domain.catalog.di.domainCatalogModule
import mmap.domain.nodes.di.domainNodesModule
import org.koin.dsl.module

val domainModule = module {
    includes(
        domainAuthModule,
        domainCatalogModule,
        domainNodesModule,
    )
}
