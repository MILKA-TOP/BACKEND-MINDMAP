package mmap.domain.nodes.di

import mmap.core.IgnoreCoverage
import mmap.domain.nodes.NodesRepository
import org.koin.dsl.module

@IgnoreCoverage
val domainNodesModule = module {
    factory {
        NodesRepository(
            mapsDataSource = get(),
            nodesRepository = get(),
        )
    }
}
