package mmap.features.nodes.models.response

import kotlinx.serialization.Serializable

@Serializable
data class NodeToggleResponseRemote(
    val nodeId: String,
    val isMarked: Boolean,
)
