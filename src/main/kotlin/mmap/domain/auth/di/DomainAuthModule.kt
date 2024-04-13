package mmap.domain.auth.di

import mmap.domain.nodes.NodesRepository
import org.koin.dsl.module

val domainAuthModule = module {
    factory {
        NodesRepository(
            mapsDataSource = get(),
            nodesRepository = get()
        )
    }
}
