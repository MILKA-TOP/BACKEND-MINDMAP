package mmap.features.nodes.di

import mmap.features.nodes.NodesModifyController
import org.koin.dsl.module

val featureNodesModule = module {
    factory {
        NodesModifyController(
            nodesRepository = get()
        )
    }
}
