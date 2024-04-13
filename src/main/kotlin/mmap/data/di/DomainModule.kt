package mmap.data.di

import mmap.data.auth.SessionsDataSource
import mmap.data.auth.UsersDataSource
import org.koin.dsl.module

val dataModule = module {
    single { SessionsDataSource() }
    single { UsersDataSource() }
}
