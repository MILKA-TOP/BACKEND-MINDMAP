package mmap.features.maps.models.response

import kotlinx.serialization.Serializable

@Serializable
data class MapsUpdateResponseRemote(
    val nodes: List<MapsKeyResponsePair> = emptyList(),
    val tests: List<MapsKeyResponsePair> = emptyList(),
    val questions: List<MapsKeyResponsePair> = emptyList(),
    val answers: List<MapsKeyResponsePair> = emptyList(),
)

@Serializable
data class MapsKeyResponsePair(val deviceId: String, val serverId: String)