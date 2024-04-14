package mmap.features.nodes.di

import mmap.core.IgnoreCoverage
import mmap.features.nodes.NodesModifyController
import org.koin.dsl.module

@IgnoreCoverage
val featureNodesModule = module {
    factory {
        NodesModifyController(
            nodesRepository = get()
        )
    }
}
