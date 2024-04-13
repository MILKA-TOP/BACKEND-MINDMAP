package mmap.domain.di

import mmap.domain.auth.di.domainAuthModule
import mmap.domain.auth.di.domainNodesModule
import org.koin.dsl.module

val domainModule = module {
    includes(
        domainAuthModule,
        domainNodesModule,
    )
}
