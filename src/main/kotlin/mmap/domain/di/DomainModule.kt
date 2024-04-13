package mmap.domain.di

import mmap.domain.auth.di.domainAuthModule
import org.koin.dsl.module

val domainModule = module {
    includes(
        domainAuthModule,
    )
}
