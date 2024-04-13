package mmap.domain.nodes.di

import mmap.domain.nodes.NodesRepository
import org.koin.dsl.module

val domainNodesModule = module {
    factory {
        NodesRepository(
            mapsDataSource = get(),
            nodesRepository = get(),
        )
    }
}
