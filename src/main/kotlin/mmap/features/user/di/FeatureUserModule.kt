package mmap.features.user.di

import mmap.features.user.UserController
import org.koin.dsl.module

val featureUserModule = module {
    factory {
        UserController(
            authRepository = get()
        )
    }
}
