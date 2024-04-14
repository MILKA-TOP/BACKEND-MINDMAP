package mmap.domain.auth.di

import mmap.domain.auth.AuthRepository
import org.koin.dsl.module


val domainAuthModule = module {
    factory {
        AuthRepository(
            sessionDataSource = get(),
            usersDataSource = get()
        )
    }
}
