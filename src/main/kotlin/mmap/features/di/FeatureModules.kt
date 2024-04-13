package mmap.features.di

import mmap.features.user.di.featureUserModule
import org.koin.dsl.module

val featureModule = module {
    includes(
        featureUserModule,
    )
}
