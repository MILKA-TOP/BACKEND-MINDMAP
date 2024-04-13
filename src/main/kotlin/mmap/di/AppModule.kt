package mmap.di

import mmap.data.di.dataModule
import mmap.domain.di.domainModule
import mmap.features.di.featureModule
import org.koin.dsl.module

val appModule = module {
    includes(
        featureModule,
        domainModule,
        dataModule,
    )
}
