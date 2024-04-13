package mmap.data.di

import mmap.data.auth.SessionsDataSource
import mmap.data.auth.UsersDataSource
import mmap.data.maps.MapsDataSource
import mmap.data.maps.NodesDataSource
import org.koin.dsl.module

val dataModule = module {
    single { SessionsDataSource() }
    single { UsersDataSource() }
    single { NodesDataSource() }
    single { MapsDataSource() }
}
