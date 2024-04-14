package mmap.di

import mmap.core.IgnoreCoverage
import mmap.data.di.dataModule
import mmap.domain.di.domainModule
import mmap.features.di.featureModule
import org.koin.dsl.module

@IgnoreCoverage
val appModule = module {
    includes(
        featureModule,
        domainModule,
        dataModule,
    )
}
